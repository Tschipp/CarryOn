#!/usr/bin/env groovy

pipeline {
    agent any
    tools {
        jdk "jdk-21"
    }
    stages {
        stage('Clean') {
            steps {
                withCredentials([file(credentialsId: 'mod_build_secrets', variable: 'ORG_GRADLE_PROJECT_secretFile')]) {
                    echo 'Cleaning Project'
                    sh 'chmod +x gradlew'
                    sh './gradlew clean'
                }
            }
        }
        stage('Build') {
            steps {
                withCredentials([file(credentialsId: 'mod_build_secrets', variable: 'ORG_GRADLE_PROJECT_secretFile')]) {
                    echo 'Building'
                    sh './gradlew build publish'
                }
            }
        }
        stage('Publish Mod') {
            when { buildingTag() }
            steps {
                withCredentials([file(credentialsId: 'mod_build_secrets', variable: 'ORG_GRADLE_PROJECT_secretFile')]) {
                    echo "Publishing tag ${env.TAG_NAME} to CurseForge and Modrinth"
                    sh './gradlew publishMod --no-configuration-cache'
                }
            }
        }
    }
    post {
        always {
            archive 'Forge/build/libs/**.jar'
            archive 'Fabric/build/libs/**.jar'
            archive 'NeoForge/build/libs/**.jar'
        }
    }
}