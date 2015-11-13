Gerrit CI reporting and visualization plugin
============================================

This plugin allow CI system to report result outcome to Gerrit. The result are visualized on change screen. Reporting can be done through SSH commands or REST API.

Database schema doesn't created automatically for now. The scripts are povided separately:
Prebuilt artifacts 

Persistense
-----------

CI data is stored in seperate database (not review DB used by Gerrit
itself). The following database dialects are currently supported:

* H2
* MySQL
* Oracle
* PostgreSQL

Database schema doesn't created automatically for now. The script is povided for H2 only for now.

```
CREATE TABLE PATCH_SET_VERIFICATIONS(
VALUE SMALLINT DEFAULT 0 NOT NULL,
GRANTED TIMESTAMP NOT NULL,
URL VARCHAR(255),
VERIFIER VARCHAR(255),
COMMENT VARCHAR(255),
CHANGE_ID INTEGER DEFAULT 0 NOT NULL,
PATCH_SET_ID INTEGER DEFAULT 0 NOT NULL,
CATEGORY_ID VARCHAR(255) DEFAULT '' NOT NULL,
PRIMARY KEY(CHANGE_ID, PATCH_SET_ID, CATEGORY_ID));
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
* Schema initialization and upgrade
* UI integration

License
-------

Apache License 2.0
