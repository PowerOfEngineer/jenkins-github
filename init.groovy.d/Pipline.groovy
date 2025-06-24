import jenkins.model.*
import org.jenkinsci.plugins.workflow.job.*
import org.jenkinsci.plugins.workflow.cps.*
import java.util.logging.Logger
import hudson.plugins.git.GitSCM
import hudson.plugins.git.UserRemoteConfig
import hudson.plugins.git.BranchSpec
import hudson.triggers.SCMTrigger

// Initialize logger for debugging and informational messages
def logger = Logger.getLogger("")

// Create a new Pipeline job.
def jobName = "Essay Pipeline"
job = Jenkins.instance.createProject(WorkflowJob.class, jobName)
job.setDefinition(new CpsFlowDefinition(createPipelineScript(), true)) // Set the pipeline script
job.addTrigger(new SCMTrigger("H/2 * * * *")) // Add SCM polling trigger to check for changes every 2 minutes
job.save() // Save the job configuration
logger.info("Pipeline Job Created Successfully with name: ${jobName}")

// Trigger the initial build of the pipeline job
job.scheduleBuild2(0)
logger.info("Initial build triggered for the newly created job.")

// This function defines the actual Jenkins pipeline script
def createPipelineScript() {
    return '''
    pipeline {
        agent any
        environment {
            // Set the location of the Kubernetes config file
            KUBECONFIG = '/var/jenkins_home/.kube/config'
        }
        stages {
            stage('Clean Workspace') {
                steps {
                    // Clean the workspace to avoid conflicts with previous builds
                    deleteDir()
                }
            }
            stage('Checkout') {
                steps {
                    // Clone the Git repository with Kubernetes manifests
                    git url: 'https://github.com/PowerOfEngineer/jenkins-github.git', 
                        branch: 'main', 
                        credentialsId: 'github-token'
                    println("pull step from fzem")
                }
            }
            stage('Apply K8s Manifests') {
                steps {
                    // Apply all Kubernetes manifests in the k8s directory
                    sh "chmod +x k8s/deploy.sh && ./k8s/deploy.sh"
                }
            }            
        }       
    }
    '''.stripIndent()
}

println("Job configured with GitSCM and pollSCM trigger every 2 minutes")
logger.info("Job configured with GitSCM and pollSCM trigger every 2 minutes")