# sync-files

Tool for synchronizing pages in Wildfly/JBoss in development environments. This tool is used for test page change at runtime and without needing to regenerate and redeploy ear/war.

Example:                
         java -jar syncfiles.jar "/home/user/DEV/teste/2.0/" "/home/user/Servers/wildfly14/standalone/tmp/vfs/deployment/" "src" "xhtml,html,css,js" 
