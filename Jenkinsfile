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
        GNUPGHOME="$WORKSPACE/.gnupg"
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
        stage('Install GPG key') {
            steps {
                withCredentials([
                        file(credentialsId: 'sonatype-private-key', variable: 'GPG_KEY_FILE'),
                        string(credentialsId: 'sonatype-private-key-passphrase', variable: 'GPG_PASSPHRASE')
                ]) {
                    sh '''
        set -e
        apt-get update && apt-get install -y gnupg2 pinentry-curses
        rm -rf "$GNUPGHOME"
        mkdir -p "$GNUPGHOME"
        chmod 700 "$GNUPGHOME"

        gpg --batch --yes --pinentry-mode loopback --import "$GPG_KEY_FILE"

        KEY_ID=$(gpg --batch --with-colons --list-secret-keys | awk -F: '/^sec/ {print $5; exit}')
        [ -n "$KEY_ID" ] || { echo "No private key imported"; exit 1; }

        echo "pinentry-mode loopback" >> "$GNUPGHOME/gpg.conf"
        echo "default-key $KEY_ID" >> "$GNUPGHOME/gpg.conf"
        echo "allow-loopback-pinentry" >> "$GNUPGHOME/gpg-agent.conf"

        # Restart agent to pick up allow-loopback-pinentry (ignore errors if agent not running)
        gpgconf --kill gpg-agent || true

        echo "GPG key loaded: $KEY_ID"
        gpg --batch --list-secret-keys
        # Store for later stages
        echo "$KEY_ID" > KEY_ID_FILE
      '''
                }
            }
        }

        stage('Build & Test') {
            when {
                expression { env.SKIP_BUILD != 'true' }
            }
            steps {
                sh 'mvn -B clean verify -Dgpg.homedir="$GNUPGHOME"'
            }
        }

        stage('Deploy to Maven Central') {
/*
            when {
                allOf {
                    branch 'master'
                    expression { env.SKIP_BUILD != 'true' }
                }
            }
*/
            steps {
                withCredentials([
                        usernamePassword(credentialsId: 'sonatype', usernameVariable: 'OSSRH_USERNAME', passwordVariable: 'OSSRH_PASSWORD'),
                        string(credentialsId: 'sonatype-private-key-passphrase', variable: 'GPG_PASSPHRASE')
                ]) {
                    sh '''
        set -e
        KEY_ID=$(cat KEY_ID_FILE)
        cat > settings.xml <<EOF
<settings>
  <servers>
    <server>
      <id>sonatype-nexus-staging</id>
      <username>${OSSRH_USERNAME}</username>
      <password>${OSSRH_PASSWORD}</password>
    </server>
    <server>
      <id>sonatype-nexus-snapshots</id>
      <username>${OSSRH_USERNAME}</username>
      <password>${OSSRH_PASSWORD}</password>
    </server>
  </servers>
</settings>
EOF
        mvn -B -s settings.xml \
          -Dgpg.keyname="$KEY_ID" \
          -Dgpg.passphrase="$GPG_PASSPHRASE" \
          -Dgpg.homedir="$GNUPGHOME" \
          -DskipTests deploy
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
