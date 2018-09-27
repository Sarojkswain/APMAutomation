
# Configuration script to create siteminder policy objects needed for registering web agent
# to local policy server

use Netegrity::PolicyMgtAPI;

$hostname = `hostname`;
chomp($hostname);
$domainname = '.ca.com';
$hostname = $hostname.$domainname;
print "Hostname = ".$hostname."\n";
$policyapi = Netegrity::PolicyMgtAPI->New();
$session = $policyapi->CreateSession("siteminder", "siteminder");
if (! defined $session) {
    print "Couldn't create API session.\n";
    exit;
}


# Create web agent
$ip="127.0.0.1";
$agentType=$session->GetAgentType("Web Agent");
$agent = $session->CreateAgent("cawebagent",$agentType,"local apache web agent",$ip);
if (! defined $agent) {
    print "FAIL - Couldn't create agent.\n";
}
else {
    print "OK - Agent created.\n";
}
# Create agent conf object
$agentconftemplate = $session->GetAgentConfig("ApacheDefaultSettings");
if (! defined $agentconftemplate) {
    print "FAIL - Couldn't get agent config template.\n";
}

$agentconf = $session->CreateAgentConfig("cawebagentconf", "local web agent config");
if (! defined $agentconf) {
    print "FAIL - Couldn't create agent config.\n";
}
else {
    print "OK - Agent config created.\n";
	@assocs = $agentconftemplate->GetAssociations();
	foreach $assoc (@assocs) {
		#print $assoc->Name()." ".$assoc->Value()." ".$assoc->Flags()."\n";
		$agentconf->AddAssociation($assoc->Name(), $assoc->Value(), $assoc->Flags());
		$agentconf->AddAssociation("DefaultAgentName", "cawebagent", 0);
		$agentconf->AddAssociation("EnableWebAgent", "yes", 0);
	}
}

# Create host config
$hostconf = $session->CreateHostConfig("cawebhost", "local host config", 1, 20, 2, 2, 60);
if (! defined $hostconf) {
    print "FAIL - Couldn't create host config.\n";
}
else {
	print "OK - Host config created.\n";
	# Add policy server to host config
	$result = $hostconf->AddServer($hostname);
	if ($result != 0) {
		print "?INFO - Couldn't add ps server to host config.\n";
	}
	else {
		print "OK - ps server added to host config created.\n";
	}
}

# is this needed? done during agent install
# Register truted host
#$trustedHost = $session->CreateTrustedHost($hostname, "127.0.0.1", "siteminder", "siteminder", "cawebhost", "aa");
#if (! defined $trustedHost) {
#    print "FAIL - Couldn't add trusted host.\n";
#}
#else {
#    print "OK - Added trusted host.\n";
#}

# Create Form authentication scheme
# Retrieve the object for the authentication scheme template
$template=$session->GetAuthScheme("HTML Form Template");
if (! defined $template) {
    print "FAIL - Couldn't get auth scheme.\n";
}
else {
	print "OK - Got auth scheme.\n";
	# Create the authentication scheme
	$authScheme = $session->CreateAuthScheme("caauthform", $template, "local ldap dir", 5, $template->CustomLib());
	if (! defined $authScheme) {
		print "FAIL - Couldn't create form authentication scheme.\n";
	}
	else {
		print "OK - Created auth scheme.\n";
		$authScheme->CustomParam("http://".$hostname.":80/siteminderagent/forms/login.fcc;ACS=0;REL=0");
		$authScheme->IsUsedByAdmin(1);
		$authScheme->IgnorePwd(1);
		$authScheme->Save();
	}
}

# Create user dir
$userDir = $session->CreateUserDir("cadir", "LDAP:", $hostname.":19999", undef, "cadir", "o=root", "(uid=", ")", undef, undef, 0, 2, 30);
if (! defined $userDir) {
    print "Couldn't create user directory.\n";
	$userDir = $session->GetUserDir("cadir");
}
# Create domain and policy
$domain = $session->CreateDomain("samldomain");
if (! defined $domain) {
    print "FAIL - Couldn't create domain.\n";
}
else {
	print "OK - Created domain.\n";
	$result = $domain->AddUserDir($userDir);
	if ($result != 0) {
		print "FAIL - Couldn't add user directory to domain.\n";
	}

	$realm = $domain->CreateRealm("cawebagentrealm", $agent, $authScheme);
	if (! defined $realm) {
		print "FAIL - Couldn't create realm.\n";
	}
	else {
		print "OK - Created realm.\n";
		$realm->Agent($agent);
		$realm->AuthScheme($authScheme);
		$rule = $realm->CreateRule("Allow all", ruleDesc, "GET,POST,PUT", "/*");
		if (! defined $rule) {
			print "Couldn't create rule.\n";
		}
		print "OK - Created rule.\n";
		$realm->ProcessAuEvents(1);
		$realm->ProtectResource(1);
		$realm->ResourceFilter("/affwebservices/redirectjsp");
		$realm->IdleTimeout(3600);
		$realm->MaxTimeout(7200);

		$policy = $domain->CreatePolicy("cawebagentpolicy");
		if (! defined $policy) {
			print "Couldn't create policy.\n";
		}
		else {
			print "OK - Created policy.\n";
			$policy->AddRule($rule);
			@users = $userDir->GetContents();
			foreach $user (@users) {
				$policy->AddUser($user);
			}
		}
	}

}
__END__

