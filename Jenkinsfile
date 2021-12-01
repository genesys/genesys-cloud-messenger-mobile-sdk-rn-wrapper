@Library('pipeline-library')
import com.genesys.jenkins.Service

def notifications = null

def isReleaseBranch() {
    return env.SHORT_BRANCH.equals('main');
}

pipeline {
  agent { label 'dev_mesos_v2' }
  options {
    quietPeriod(480)
    disableConcurrentBuilds()
  }

  environment {
    NPM_UTIL_PATH = "npm-utils"
    REPO_DIR = "repo"
    SHORT_BRANCH = env.GIT_BRANCH.replaceFirst(/^origin\//, '');
    NPM_TOKEN = credentials('2844c47b-19b8-4c5f-b901-190de49c0883')
  }

  tools {
    nodejs 'NodeJS 12.13.0'
  }

  stages {
    stage('Setup mailing list parameter') {
      steps {
        script {
          properties([
            parameters([
              string(
                defaultValue: '',
                name: 'EMAIL_LIST',
                trim: true
              )
            ])
          ])
        }
      }
    }
    stage('Import notifications lib') {
      steps {
        script {
          // clone pipelines repo
          dir('pipelines') {
            git branch: 'master',
                url: 'git@bitbucket.org:inindca/pipeline-library.git',
                changelog: false

            notifications = load 'src/com/genesys/jenkins/Notifications.groovy'
          }
        }
      }
    }

    stage('Checkout') {
      steps {
        deleteDir()
        dir(env.REPO_DIR) {
          checkout scm
          // Make a local branch so we can work with history and push (there's probably a better way to do this)
          sh "git checkout -b ${env.SHORT_BRANCH}"
        }
      }
    }

    stage('Avoid Build Loop') {
      steps {
        script {
          dir(env.REPO_DIR) {
            def lastCommit = sh(script: 'git log -n 1 --format=%s', returnStdout: true).trim()
            if (lastCommit.startsWith('chore(release)')) {
              currentBuild.description = 'Skipped'
              currentBuild.result = 'ABORTED'
              error('Last commit was a release, exiting build process.')
            }
          }
        }
      }
    }

    stage('Prep') {
      steps {
        sh "git clone --single-branch -b master --depth=1 git@bitbucket.org:inindca/npm-utils.git ${env.NPM_UTIL_PATH}"
      }
    }

    stage('Publish Library') {
      when {
        expression { isReleaseBranch()  }
      }
      steps {
          dir(env.REPO_DIR) {
          sh '''
              echo "registry=https://registry.npmjs.org" > ./.npmrc
              echo "//registry.npmjs.org/:_authToken=${NPM_TOKEN}" >> ./.npmrc
          '''
          sh "${env.WORKSPACE}/${env.NPM_UTIL_PATH}/scripts/auto-version-bump.sh"
          
          // Do not include the npm-utils directory or the publish credentials in the published package.
          sh '''
              echo "npm-utils" >> .npmignore
              npm publish 1>&2
          '''
          sshagent (credentials: ['3aa16916-868b-4290-a9ee-b1a05343667e']) {
            sh "git push --tags -u origin ${env.SHORT_BRANCH}"
          }
        }
      }
    }
  }

  post {
    fixed {
      script {
        notifications.emailResults(params.EMAIL_LIST)
      }
    }

    failure {
      script {
        notifications.emailResults(params.EMAIL_LIST)
      }
    }
  }
}
