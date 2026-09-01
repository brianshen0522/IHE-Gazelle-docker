<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
  <sch:title>Demo Patient Record rules</sch:title>
  <sch:pattern>
    <sch:rule context="/PatientRecord/Patient">
      <sch:assert test="matches(BirthTime, '^[0-9]{8}$')">BirthTime must be formatted as YYYYMMDD.</sch:assert>
      <sch:assert test="Gender = 'M' or Gender = 'F' or Gender = 'O'">Gender must be one of M, F or O.</sch:assert>
      <sch:assert test="string-length(normalize-space(Name)) &gt; 0">Patient Name must not be empty.</sch:assert>
    </sch:rule>
  </sch:pattern>
</sch:schema>
