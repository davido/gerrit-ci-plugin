
@PLUGIN@ verify
=====================

NAME
----
@PLUGIN@ verify - Apply verification to one or more patch sets

SYNOPSIS
--------
>     ssh -p <port> <host> @PLUGIN@ verify
>      [--project <PROJECT> | -p <PROJECT>]
>      [--branch <BRANCH> | -b <BRANCH>]
>      [--verification <PARAMETERS> | -v <PARAMETERS>]
>      {COMMIT | CHANGEID,PATCHSET}...


DESCRIPTION
-----------
Updates a specified patchset or commit with a verification,
sending out email notifications and updating the database.

To report the outcome for multiple jobs at once, the verification
parameter can be used multiple times.

Patch sets may be specified in 'CHANGEID,PATCHSET' format, such as
'8242,2', or 'COMMIT' format.

If a patch set is specified with the 'COMMIT' format, the complete
or abbreviated commit SHA-1 may be used.  If the same commit is available
in multiple projects the `--project` option may be used to limit where
Gerrit searches for the change to only the contents of the specified project.
If the same commit is available in multiple branches the `--branch` option
may be used to limit where Gerrit searches for changes to only the specified
branch.


OPTIONS
-------

--project
-p
> Name of the project the intended changes are contained
> within.  This option must be supplied before the commit
> SHA-1 in order to take effect.

--branch
-b
> Name of the branch the intended changes are contained
> within.  This option must be supplied before the commit
> SHA-1 in order to take effect.

--verification
-v
> The key=value pair of [VerifyInput](rest-api-changes.html#verify-input)
> parameters separated by '|' character.
 
--help
-h
> Display usage information.


ACCESS
------
Any user who has configured an SSH key.

SCRIPTING
---------
This command is intended to be used in scripts.

EXAMPLES
--------

Verified gate-horizon-pep8 test with vote=+1 on the change with commit 14a95001c.
__Notice__ two levels of quoting are required, one for the local shell, and
another for the argument parser inside the Gerrit server.


>     $ ssh -p 29418 review.example.com @PLUGIN@ verify --verification
>      "'category=gate-horizon-pep8
>      |value=1
>      |url=https://ci.host.com/jobs/pep8/4711
>      |verifier=Jenkins
>      |comment=Non Voting'"
>      14a95001c


SEE ALSO
--------

* [Access Controls](../../../Documentation/access-control.html)
* [Command Line Tools](../../../Documentation/cmd-index.html)

GERRIT
------
Part of [Gerrit Code Review](../../Documentation/index.html)
