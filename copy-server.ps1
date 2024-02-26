[xml]$mvnSettings = Get-Content ~\.m2\settings.xml
$servers = $mvnSettings.settings.servers

foreach ($server in $servers.ChildNodes) {
    if ($server.id -eq 'github') {
        $newServer = $server.Clone()
        $newServer.id = 'rewh-github'
        $servers.AppendChild($newServer)
        break
    }
}

$mvnSettings.Save('~/.m2/settings.xml')