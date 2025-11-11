@Library ("jenkins_library") _
clearWorkspaceAsRoot()
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
        apt-get update && apt-get install -y pinentry-curses
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

/*
        stage('Build & Test') {
            when {
                expression { env.SKIP_BUILD != 'true' }
            }
            steps {
                sh 'mvn -B clean verify -Dgpg.homedir="$GNUPGHOME"'
            }
        }
*/

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
                        string(credentialsId: 'sonatype-deployment-token', variable: 'SONATYPE_TOKEN'),
                        string(credentialsId: 'sonatype-private-key-passphrase', variable: 'GPG_PASSPHRASE'),
                        usernamePassword(credentialsId: 'github-token', passwordVariable: 'GITHUB_TOKEN', usernameVariable: 'GIT_USERNAME')
                ]) {
                    sh '''
        set -e
        export GIT_TERMINAL_PROMPT=0
        KEY_ID=$(cat KEY_ID_FILE)
        BRANCH="${BRANCH_NAME:-master}"

        git checkout -B "$BRANCH" "origin/$BRANCH" || git checkout -B "$BRANCH"
        git config user.name "jenkins-ci"
        git config user.email "ci@blazemeter.com"

        # Configure credential helper
        mkdir -p ~/.config
        printf "https://%s:%s@github.com\\n" "$GIT_USERNAME" "$GITHUB_TOKEN" > ~/.git-credentials
        git config --global credential.helper store

        # Set remote without inline credentials
        git remote set-url origin https://github.com/Blazemeter/taurus-java-helpers.git
        git fetch origin
        git status

        cat > settings.xml <<EOF
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>LOZXAS</username>
      <password>${SONATYPE_TOKEN}</password>
    </server>
  </servers>
</settings>
EOF

        mvn -B -s settings.xml \
          -Dgpg.keyname="$KEY_ID" \
          -Dgpg.passphrase="$GPG_PASSPHRASE" \
          -Dgpg.homedir="$GNUPGHOME" \
          -DskipTests release:prepare release:perform
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
