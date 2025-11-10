pipeline {
    agent {
        docker {
            image 'us.gcr.io/verdant-bulwark-278/jenkins-docker-agent:master.latest'
            args "-u root -v /var/run/docker.sock:/var/run/docker.sock"
        }
    }

    environment {
        MAVEN_OPTS = '-Dmaven.test.redirectTestOutputToFile=true'
        SKIP_BUILD = 'false'
    }

    options {
        timestamps()
        ansiColor('xterm')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detect Version Bump') {
            steps {
                script {
                    def msg = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
                    if (msg =~ /chore: bump version to .+/) {
                        echo "Version bump commit detected: '${msg}'. Skipping build."
                        env.SKIP_BUILD = 'true'
                    }
                }
            }
        }
        stage("Install GPG key") {
            steps {
                withCredentials([
                        file(credentialsId: 'sonatype-private-key', variable: 'GPG_KEY_FILE')
                ]) {
                    sh '''
                        apt-get update && apt-get install -y gnupg2
                        gpg --batch --import "$GPG_KEY_FILE"
                        KEY_ID=$(gpg --list-secret-keys --with-colons | awk -F: '/^sec/ {print $5; exit}')

                        if [ -z "$KEY_ID" ]; then
                          echo "No private key found!" >&2
                          exit 1
                        fi

                        # Trust key so maven-gpg-plugin won't fail
                        echo -e "5\\ny\\n" | gpg --command-fd 0 --expert --edit-key $KEY_ID trust quit

                        # Make it the default key
                        echo "default-key $KEY_ID" >> ~/.gnupg/gpg.conf
                        echo "use-agent" >> ~/.gnupg/gpg.conf

                        # Show confirmation
                        gpg --list-secret-keys
                        '''
                }
            }
        }

        stage('Build & Test') {
            when {
                expression { env.SKIP_BUILD != 'true' }
            }
            steps {
                sh 'mvn -B clean verify'
            }
        }

        stage('Deploy to Maven Central') {
            when {
                allOf {
                    branch 'master'
                    expression { env.SKIP_BUILD != 'true' }
                }
            }
            steps {
                withCredentials([
                        usernamePassword(credentialsId: 'sonatype', usernameVariable: 'OSSRH_USERNAME', passwordVariable: 'OSSRH_PASSWORD'),
                        string(credentialsId: 'sonatype-private-key-passphrase', variable: 'GPG_PASSPHRASE')
                ]) {
                    sh '''
                        cat > $WORKSPACE/settings.xml <<EOF
<settings>
  <servers>
    <server>
      <id>sonatype-nexus-staging</id>
      <username>${OSSRH_USERNAME}</username>
      <password>${OSRRH_PASSWORD}</password>
    </server>
  </servers>
</settings>
EOF
                    #    mvn -B -s $WORKSPACE/settings.xml -Dgpg.passphrase="$GPG_PASSPHRASE" -DskipTests deploy
                    '''
                }
            }
        }

        stage('Skip Notice') {
            when {
                expression { env.SKIP_BUILD == 'true' }
            }
            steps {
                echo 'Build skipped due to version bump commit.'
            }
        }
    }

    post {
        success {
            echo 'Pipeline finished.'
        }
        failure {
            echo 'Pipeline failed.'
        }
    }
}
