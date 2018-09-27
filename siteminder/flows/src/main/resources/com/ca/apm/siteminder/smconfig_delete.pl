#!/usr/bin/perl

use Netegrity::PolicyMgtAPI;

$hostname = `hostname`;
$policyapi = Netegrity::PolicyMgtAPI->New();
$session = $policyapi->CreateSession("siteminder", "siteminder");
if ($session == undef) {
    print "FAIL - Couldn't create API session.\n";
	exit;
}

# Create agent conf object
$agentconf =$session->GetAgentConfig("cawebagentconf"); 
if ($agentconf == undef) {
	print "FAIL - Couldn't get agent config.\n";
}
else {
	$result = $session->DeleteAgentConfig($agentconf);
	if ($result != 0) {
		print "FAIL - Couldn't delete agent config .\n";
	}
	else {
		print "OK - Agent config deleted.\n";
	}
}
# Create host config

$hostconf = $session->GetHostConfig("cawebhost");
if ($hostconf == undef) {
	print "FAIL - Couldn't get host config.\n";
}
else {
	$result = $session->DeleteHostConfig($hostconf);
	if ($result != 0) {
		print "FAIL - Couldn't delete host config.\n";
	}
	else {
		print "OK - Host config deleted.\n";
	}
}

#$trustedHost = $session->GetTrustedHost($hostname);
#if ($trustedHost == undef) {
#	print "FAIL - Couldn't get trusted host.\n";
#}
#else {
#	$result = $session->DeleteTrustedHost($trustedHost);
#	if ($result != 0) {
#		print "FAIL - Couldn't delete trusted host.\n";
#	}
#	else {
#		print "OK - Trusted host deleted.\n";
#	}
#}

	
# Create domain and policy
$domain = $session->GetDomain("samldomain");
if ($domain == undef) {
	print "FAIL - Couldn't get domain.\n";
}
else {
	$result = $session -> DeleteDomain($domain);
	if ($result != 0) {
		print "FAIL - Couldn't delete domain.\n";
	}
	else {
		print "OK - Domain deleted.\n";
	}
}

# Delete web agent
$agent=$session->GetAgent("cawebagent");
if ($agent == undef) {
	print "FAIL - Couldn't get agent.\n";
}
else {
	$result = $session->DeleteAgent($agent);
	if ($result != 0) {
		print "FAIL - Couldn't delete agent.\n";
	}
	else {
		print "OK - Agent deleted.\n";
	}
}

# Delete Form authentication scheme
$template=$session->GetAuthScheme("caauthform");
if ($template == undef) {
	print "FAIL - Couldn't get auth scheme.\n";
}
else {
	$result = $session->DeleteAuthScheme($template);
	if ($result != 0) {
		print "FAIL - Couldn't delete auth scheme.\n";
	}
	else {
		print "OK - Auth scheme deleted.\n";
	}
}

$userDir = $session->GetUserDir("cadir");
if ($userDir == undef) {
	print "FAIL - Couldn't get auth scheme.\n";
}
else {
	$result = $session->DeleteUserDir($userdir);
	if ($result != 0) {
		print "FAIL - Couldn't delete user dir.\n";
	}
	else {
		print "OK - User dir deleted.\n";
	}
}
__END__

