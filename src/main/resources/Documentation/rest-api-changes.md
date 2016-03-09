<style>
  table{
      border-collapse: collapse;
      border-spacing: 0;
      border:2px solid #000000;
  }
  
  th{
      border:2px solid #000000;
  }
  
  td{
      border:1px solid #000000;
  }
</style>

@PLUGIN@ - /changes/ REST API
==============================

This page describes the '/changes/' REST endpoints that are added by
the @PLUGIN@ plugin.

Please also take note of the general information on the
[REST API](../../../Documentation/rest-api.html).

<a id="plugin-endpoints"> @PLUGIN@ Endpoints
--------------------------------------------

### <a id="get-verifications"> Get Verifications

__GET__ /changes/{change-id}/revisions/{revision-id}/review

Gets the [verifications](#verification-info) for a change.  Please refer to the
general [changes rest api](../../../Documentation/rest-api-changes.html#get-review)
for additional info on this request.

#### Request

```
  GET /changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/revisions/674ac754f91e64a0efb8087e59a176484bd534d1/review HTTP/1.0
```

#### Response

```
  HTTP/1.1 200 OK
  Content-Disposition: attachment
  Content-Type: application/json; charset=UTF-8

  )]}'
  {
    "id": "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940",
    "project": "myProject",
    "branch": "master",
    "change_id": "I8473b95934b5732ac55d26311a706c9c2bde9940",
    "subject": "Implementing Feature X",
    "status": "NEW",
    "created": "2013-02-01 09:59:32.126000000",
    "updated": "2013-02-21 11:16:36.775000000",
    "mergeable": true,
    "insertions": 34,
    "deletions": 45,
    "_number": 3965,
    "owner": {
      "_account_id": 1000096,
      "name": "John Doe",
      "email": "john.doe@example.com"
    },
    "labels": {
      "Verified": {
        "all": [
          {
            "value": 0,
            "_account_id": 1000096,
            "name": "John Doe",
            "email": "john.doe@example.com"
          },
        ],
        "values": {
          "-1": "Fails",
          " 0": "No score",
          "+1": "Verified"
        }
      },
      "Code-Review": {
        "all": [
          {
            "value": 1,
            "_account_id": 1000097,
            "name": "Jane Roe",
            "email": "jane.roe@example.com"
          }
        ]
        "values": {
          "-2": "This shall not be merged",
          "-1": "I would prefer this is not merged as is",
          " 0": "No score",
          "+1": "Looks good to me, but someone else must approve",
          "+2": "Looks good to me, approved"
        }
      }
    },
    "permitted_labels": {
      "Verified": [
        "-1",
        " 0",
        "+1"
      ],
      "Code-Review": [
        "-2",
        "-1",
        " 0",
        "+1",
        "+2"
      ]
    },
    "removable_reviewers": [
      {
        "_account_id": 1000097,
        "name": "Jane Roe",
        "email": "jane.roe@example.com"
      }
    ],
    "current_revision": "674ac754f91e64a0efb8087e59a176484bd534d1",
    "revisions": {
      "674ac754f91e64a0efb8087e59a176484bd534d1": {
      "_number": 2,
      "ref": "refs/changes/65/3965/2",
      "fetch": {
        "http": {
          "url": "http://gerrit/myProject",
          "ref": "refs/changes/65/3965/2"
        }
      },
      "verifications": {
        "gate-horizon-pep8": {
            "comment": "Non Voting",
            "url": "https://ci.host.com/jobs/pep8/4711",
            "value": -1
            "verifier": "Jenkins",
        }
      }
    }
  }
```

### <a id="post-verify"> Post Verify

__POST__ /changes/{change-id}/revisions/{revision-id}/verify'

Posts a verification on a patchset revision.

The verification must be provided in the request body as a
[VerifyInput](#verify-input) entity.

#### Request

```
  POST /changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/revisions/674ac754f91e64a0efb8087e59a176484bd534d1/verify HTTP/1.0
  Content-Type: application/json;charset=UTF-8

```

#### Example

Verified gate-horizon-pep8 test with vote=+1 on the change with commit 14a95001c.
_Notice_ two levels of quoting are required, one for the local shell, and
another for the argument parser inside the Gerrit server.

```
curl -X POST --digest --user joe:secret --data-binary
@verification_data.txt --header "Content-Type: application/json; charset=UTF-8" 
http://localhost:8080/a/changes/1000/revisions/4d5fda7e653534b1709883d96264910fab03ddbb/verify

$ cat verification_data.txt
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

<a id="json-entities">JSON Entities
-----------------------------------

### <a id="verify-input"></a>VerifyInput

The `VerifyInput` entity contains information for adding a verification
to a revision.


|Field Name  |     |Description|
|:-----------|:----|:----------|
|category    |required|The name of the category to be added as a verification|
|value       |required|The value associated with the category|
|comment     |optional|The comment associated with the category|
|url         |optional|The url associated with the category|
|verifier    |optional|The user that verified the revision|



### <a id="revision-info"></a>RevisionInfo

The `RevisionInfo` entity contains information about a patch set.
Not all fields are returned by default.  Additional fields can
be obtained by adding `o` parameters as described in
[Query Changes](../../../Documentation/rest-api-changes.html#list-changes)

|Field Name    |    |Description |
|:-------------|:---|:-----------|
|verifications |optional|The verifications on the patchset as a list of `VerificationInfo` entities|


### <a id="verification-info"></a>VerificationInfo

The `VerificationInfo` entity describes a verification on a patch set.

|Field Name |Description|
|:----------|:----------|
|comment    |A short comment about about this verification|
|url        |The URL for this verification|
|value      |The value for this verification|
|verifier   |The user that reported this verification|



SEE ALSO
--------

* [Change related REST endpoints](../../../Documentation/rest-api-changes.html)
* [Plugin Development](../../../Documentation/dev-plugins.html)
* [REST API Development](../../../Documentation/dev-rest-api.html)

GERRIT
------
Part of [Gerrit Code Review](../../../Documentation/index.html)
