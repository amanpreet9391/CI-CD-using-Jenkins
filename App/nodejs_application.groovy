job("Nodejs Application"){
scm{
    git('git://github.com/amanpreet9391/Smallcase-Assignment.git'){ node ->
    node / gitConfigName('amanpreet9391')
    node / gitConfigEmail('amanpreet9391@gmail.com')
    }
}

triggers{
    scm('H/60 * * * *')
}
wrappers{
    nodejs('Nodejs')
    
}
steps{
    dir("App"){
        sh "pwd"
    }
    shell('npm install')
    
    dockerBuildAndPublish{
        
        repositoryName('amanpreet9391/simple-nodejs-app')
       tag('${GIT_REVISION,length=9}')
       registryCredentials('dockerhub')
             forcePull(false)
             forceTag(false)
             createFingerprints(false)
             skipDecorate()

        
    }
}






}