#!/usr/bin/perl

use Netegrity::PolicyMgtAPI;

$hostname = `hostname`;
$policyapi = Netegrity::PolicyMgtAPI->New();
$session = $policyapi->CreateSession("siteminder", "siteminder");
if ($session == undef) {
    print "Couldn't create API session.\n";
}

@items = $session->GetAllHostConfigs();
foreach $item (@items)  {
	print $item."\n";
	@ss = $item->GetAllServers();
	foreach $s (@ss)  {
		print "\t".$s->GetServerAddress()."\n";
	}
}

#Directory
@items = $session->GetAllUserDirs();
foreach $item (@items)  {
	print $item." ".$item->Name()."\n";
	print "\t".$item->SearchScope()."\n";
	print "\t".$item->GetNamespace()."\n";
}