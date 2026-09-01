```
title: Release note
subtitle: Message capture - 0.1.0
toolversion: 0.1.0
releasedate: 2024-05-27
```

# Release note

## 0.1.0

_Release date: 2024-05-27_

Context : Gazelle datahouse UI

**Stories**

- \[[GDH-13](https://gazelle.ihe.net/jira/browse/GDH-13)\] [PROXY 02][01] Flat list of the messages recorded (no pagination, no filter, no ordering)
- \[[GDH-17](https://gazelle.ihe.net/jira/browse/GDH-17)\] [PROXY 02][02] Pagination of results (fix number of entries)
- \[[GDH-20](https://gazelle.ihe.net/jira/browse/GDH-20)\] [PROXY 02][03] Filtering and Sorting by Proxy Port
- \[[GDH-21](https://gazelle.ihe.net/jira/browse/GDH-21)\] [PROXY 02][04] Filter and sort by channel type
- \[[GDH-28](https://gazelle.ihe.net/jira/browse/GDH-28)\] [PROXY 02][05] Filter and sort by timestamp
- \[[GDH-30](https://gazelle.ihe.net/jira/browse/GDH-30)\] [PROXY 02][06] Message Filtering by Hostname or IP Address
- \[[GDH-32](https://gazelle.ihe.net/jira/browse/GDH-32)\] [PROXY 02][07] Filter and sort message according to their type
- \[[GDH-35](https://gazelle.ihe.net/jira/browse/GDH-35)\] [PROXY 02][08] Display the selected message with connection summary and raw content
- \[[GDH-56](https://gazelle.ihe.net/jira/browse/GDH-56)\] [PROXY 02][09] Access detailed message in a new page (same tab)
- \[[GDH-57](https://gazelle.ihe.net/jira/browse/GDH-57)\] [PROXY 02][10] Go back to list of messages
- \[[GDH-34](https://gazelle.ihe.net/jira/browse/GDH-34)\] [PROXY 02][11] Set back all filters to default
- \[[GDH-58](https://gazelle.ihe.net/jira/browse/GDH-58)\] [PROXY 02][12] Display error messages
- \[[GDH-59](https://gazelle.ihe.net/jira/browse/GDH-59)\] [PROXY 02][13] Inform user about decoding error
- \[[GDH-60](https://gazelle.ihe.net/jira/browse/GDH-60)\] [PROXY 02][14] Display payload as Hex dump
- \[[GDH-61](https://gazelle.ihe.net/jira/browse/GDH-61)\] [PROXY 02][15] Verify conformance
- \[[GDH-62](https://gazelle.ihe.net/jira/browse/GDH-62)\] [PROXY 02][16] Display reference to conformance reports
- \[[GDH-63](https://gazelle.ihe.net/jira/browse/GDH-63)\] [PROXY 02][17] Access conformance report
- \[[GDH-64](https://gazelle.ihe.net/jira/browse/GDH-64)\] [PROXY 02][18] Download payload
- \[[GDH-65](https://gazelle.ihe.net/jira/browse/GDH-65)\] [PROXY 02][19] Display message summary on the right-hand side
- \[[GDH-66](https://gazelle.ihe.net/jira/browse/GDH-66)\] [PROXY 02][20] Overview for HTTP messages
- \[[GDH-67](https://gazelle.ihe.net/jira/browse/GDH-67)\] [PROXY 02][21] Overview for DICOM messages
- \[[GDH-68](https://gazelle.ihe.net/jira/browse/GDH-68)\] [PROXY 02][22] Overview for HL7 messages
- \[[GDH-69](https://gazelle.ihe.net/jira/browse/GDH-69)\] [PROXY 02][23] Overview for SYSLOG messages
- \[[GDH-71](https://gazelle.ihe.net/jira/browse/GDH-71)\] [PROXY 02][25] Connection
- \[[GDH-72](https://gazelle.ihe.net/jira/browse/GDH-72)\] [PROXY 02][26] Copy permanent link to message details page
- \[[GDH-80](https://gazelle.ihe.net/jira/browse/GDH-80)\] [PROXY 02][31] Share the link to a list of filtered messages
- \[[GDH-81](https://gazelle.ihe.net/jira/browse/GDH-81)\] [PROXY 02][32] Display DICOM DIMSE payload
- \[[GDH-82](https://gazelle.ihe.net/jira/browse/GDH-82)\] [PROXY 02][33] Display Syslog payload
- \[[GDH-83](https://gazelle.ihe.net/jira/browse/GDH-83)\] [PROXY 02][34] Display HL7V2 payload
- \[[GDH-84](https://gazelle.ihe.net/jira/browse/GDH-84)\] [PROXY 02][35] Display HTTP payload
- \[[GDH-85](https://gazelle.ihe.net/jira/browse/GDH-85)\] [PROXY 02][36] Show/Hide line numbers
- \[[GDH-86](https://gazelle.ihe.net/jira/browse/GDH-86)\] [PROXY 02][37] Prettify XML content

**Bugfixes**

- \[[PROXY-346](https://gazelle.ihe.net/jira/browse/PROXY-346)\] Sort is done only in the current page
- \[[PROXY-349](https://gazelle.ihe.net/jira/browse/PROXY-349)\] AM-PM and GMT is not close to the time
- \[[PROXY-340](https://gazelle.ihe.net/jira/browse/PROXY-340)\] Date from filter not always good
- \[[PROXY-342](https://gazelle.ihe.net/jira/browse/PROXY-342)\] No indication about the number of messages captured
- \[[PROXY-347](https://gazelle.ihe.net/jira/browse/PROXY-347)\] Filter is reset after click on back button
- \[[PROXY-350](https://gazelle.ihe.net/jira/browse/PROXY-350)\] Subject of certificate in TLS not present in detail page
- \[[PROXY-351](https://gazelle.ihe.net/jira/browse/PROXY-351)\] No description for HTTP rewrite in detail page
- \[[PROXY-354](https://gazelle.ihe.net/jira/browse/PROXY-354)\] DICOM messages downloaded are not usable
- \[[PROXY-355](https://gazelle.ihe.net/jira/browse/PROXY-355)\] Sort messages by timestamp
- \[[PROXY-356](https://gazelle.ihe.net/jira/browse/PROXY-356)\] Error display in detail page is not correct
- \[[PROXY-357](https://gazelle.ihe.net/jira/browse/PROXY-357)\] Access a message within the same connection by index impossible
- \[[PROXY-363](https://gazelle.ihe.net/jira/browse/PROXY-363)\] Sort on timestamp lead to error 500
- \[[PROXY-364](https://gazelle.ihe.net/jira/browse/PROXY-364)\] Filter with unknow IP in receiver field lead to a 404 error
- \[[GDH-102](https://gazelle.ihe.net/jira/browse/GDH-102)\] Display Command Field in Overview panel
