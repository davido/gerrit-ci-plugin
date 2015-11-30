Gerrit CI reporting and visualization plugin
============================================

This plugin allows CI system to report result outcome to Gerrit. The result is visualized on change screen pet patch set. Reporting can be done through SSH command or REST API.

Persistense
-----------

CI data is stored in seperate CI database (not review DB used by Gerrit
itself). The following database dialects are currently supported:

* Derby
* H2
* MySQL
* Oracle
* PostgreSQL

Schema initialization
---------------------

Database is initialized and the schema is created with init plugin step:

```
*** SQL Database for CI plugin
*** 

Database server type           [h2]: ?
       Supported options are:
         derby
         h2
         mysql
         oracle
         postgresql
Database server type           [h2]: h2

Initialized <gerrit-site>
```

Schema upgrade
--------------

Schema upgrade takes place in init plugin step:

```
*** SQL Database for CI plugin
*** 

Database server type           [h2]: 

Upgrading schema to 2 ...
Migrating data to schema 2 ...
```

Example for SSH command
-----------------------

```
ssh gerritd ci verify --verification "'category=gate-horizon-pep8|value=1|url=https://ci.host.com/jobs/pep8/4711|verifier=Jenkins|comment=Non Voting'" a14825a6e9c75b68c6be486ec2b8b6fed43b8858
```

Example for REST API
--------------------

```
curl -X POST --digest --user jow:secret --data-binary
@post-verify.txt --header "Content-Type: application/json;
charset=UTF-8"
http://localhost:8080/a/changes/1/revisions/4d5fda7e653534b1709883d96264910fab03ddbb/verify

$ cat post-verify.txt
{
  "verifications": {
    "gate-puma-pep8": {
      "value": -1,
      "url": "https://ci.host.com/jobs/pep8/1711",
      "comment": "Failed",
      "verifier": "Jenkins"
    }
  }
}

```

TODO
----

* Documentation
* UI integration

License
-------

Apache License 2.0
