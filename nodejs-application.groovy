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
    shell('npm install')
}






}