# Riscoss Data Collectors (RDC)

[![XWiki labs logo](https://labs.xwiki.com/xwiki/bin/download/Developments/Xlabs/xwiki-labs-project.png "XWiki labs")](https://labs.xwiki.com/xwiki/bin/view/Main/WebHome)

Risk Data Collectors (RDC) are the main agents that are in charge of collecting risk drivers about the entities that are taken into account for the risk analysis, and to send it to the Risk Data Repository to be made available to the other components within the platform.

Risk Data Collectors are independent components that can be implemented with any technology (e.g., by using scripting languages like Ruby, Python or Node.js or even with older languages like C).

Risk Data Collectors fall into two sub-groups. There are the so-called "external" Risk Data Collectors, i.e. those which are invoked from outside of the Domain Manager, perhaps by third parties such as Open Source Community members who want to provide data about their project to the RISCOSS Platform and there are the so-called "internal" Risk Data Collectors which are included in the platform and made available to be activated and configured when a project is registered in the Domain Manager and then are invoked directly and periodically from the Data Collector Manager within the Domain Manager.

This repository contains the "external" Risk Data Collectors, which store their data using the REST API.

Please see https://github.com/RISCOSS/riscoss-risk-modeling for detailed information about the models.

Detailed information about RISCOSS Risk Data Collectors in this repository wiki
