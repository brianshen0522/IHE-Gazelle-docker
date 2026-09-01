<?xml version="1.0" encoding="UTF-8"?><xsl:stylesheet xmlns:iso="http://purl.oclc.org/dsdl/schematron" xmlns:saxon="http://saxon.sf.net/" xmlns:schold="http://www.ascc.net/xml/schematron" xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"><!--Implementers: please note that overriding process-prolog or process-root is 
    the preferred method for meta-stylesheets to use where possible. -->
<xsl:param name="archiveDirParameter"/><xsl:param name="archiveNameParameter"/><xsl:param name="fileNameParameter"/><xsl:param name="fileDirParameter"/><xsl:variable name="document-uri"><xsl:value-of select="document-uri(/)"/></xsl:variable>

<!--PHASES-->


<!--PROLOG-->
<xsl:output xmlns:svrl="http://purl.oclc.org/dsdl/svrl" indent="yes" method="xml" omit-xml-declaration="no" standalone="yes"/>

<!--XSD TYPES FOR XSLT2-->


<!--KEYS AND FUNCTIONS-->


<!--DEFAULT RULES-->


<!--MODE: SCHEMATRON-SELECT-FULL-PATH-->
<!--This mode can be used to generate an ugly though full XPath for locators-->
<xsl:template match="*" mode="schematron-select-full-path"><xsl:apply-templates mode="schematron-get-full-path" select="."/></xsl:template>

<!--MODE: SCHEMATRON-FULL-PATH-->
<!--This mode can be used to generate an ugly though full XPath for locators-->
<xsl:template match="*" mode="schematron-get-full-path"><xsl:apply-templates mode="schematron-get-full-path" select="parent::*"/><xsl:text>/</xsl:text><xsl:choose><xsl:when test="namespace-uri()=''"><xsl:value-of select="name()"/></xsl:when><xsl:otherwise><xsl:text>*:</xsl:text><xsl:value-of select="local-name()"/><xsl:text>[namespace-uri()='</xsl:text><xsl:value-of select="namespace-uri()"/><xsl:text>']</xsl:text></xsl:otherwise></xsl:choose><xsl:variable name="preceding" select="count(preceding-sibling::*[local-name()=local-name(current())                                   and namespace-uri() = namespace-uri(current())])"/><xsl:text>[</xsl:text><xsl:value-of select="1+ $preceding"/><xsl:text>]</xsl:text></xsl:template><xsl:template match="@*" mode="schematron-get-full-path"><xsl:apply-templates mode="schematron-get-full-path" select="parent::*"/><xsl:text>/</xsl:text><xsl:choose><xsl:when test="namespace-uri()=''">@<xsl:value-of select="name()"/></xsl:when><xsl:otherwise><xsl:text>@*[local-name()='</xsl:text><xsl:value-of select="local-name()"/><xsl:text>' and namespace-uri()='</xsl:text><xsl:value-of select="namespace-uri()"/><xsl:text>']</xsl:text></xsl:otherwise></xsl:choose></xsl:template>

<!--MODE: SCHEMATRON-FULL-PATH-2-->
<!--This mode can be used to generate prefixed XPath for humans-->
<xsl:template match="node() | @*" mode="schematron-get-full-path-2"><xsl:for-each select="ancestor-or-self::*"><xsl:text>/</xsl:text><xsl:value-of select="name(.)"/><xsl:if test="preceding-sibling::*[name(.)=name(current())]"><xsl:text>[</xsl:text><xsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1"/><xsl:text>]</xsl:text></xsl:if></xsl:for-each><xsl:if test="not(self::*)"><xsl:text/>/@<xsl:value-of select="name(.)"/></xsl:if></xsl:template><!--MODE: SCHEMATRON-FULL-PATH-3-->
<!--This mode can be used to generate prefixed XPath for humans 
	(Top-level element has index)-->
<xsl:template match="node() | @*" mode="schematron-get-full-path-3"><xsl:for-each select="ancestor-or-self::*"><xsl:text>/</xsl:text><xsl:value-of select="name(.)"/><xsl:if test="parent::*"><xsl:text>[</xsl:text><xsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1"/><xsl:text>]</xsl:text></xsl:if></xsl:for-each><xsl:if test="not(self::*)"><xsl:text/>/@<xsl:value-of select="name(.)"/></xsl:if></xsl:template>

<!--MODE: GENERATE-ID-FROM-PATH -->
<xsl:template match="/" mode="generate-id-from-path"/><xsl:template match="text()" mode="generate-id-from-path"><xsl:apply-templates mode="generate-id-from-path" select="parent::*"/><xsl:value-of select="concat('.text-', 1+count(preceding-sibling::text()), '-')"/></xsl:template><xsl:template match="comment()" mode="generate-id-from-path"><xsl:apply-templates mode="generate-id-from-path" select="parent::*"/><xsl:value-of select="concat('.comment-', 1+count(preceding-sibling::comment()), '-')"/></xsl:template><xsl:template match="processing-instruction()" mode="generate-id-from-path"><xsl:apply-templates mode="generate-id-from-path" select="parent::*"/><xsl:value-of select="concat('.processing-instruction-', 1+count(preceding-sibling::processing-instruction()), '-')"/></xsl:template><xsl:template match="@*" mode="generate-id-from-path"><xsl:apply-templates mode="generate-id-from-path" select="parent::*"/><xsl:value-of select="concat('.@', name())"/></xsl:template><xsl:template match="*" mode="generate-id-from-path" priority="-0.5"><xsl:apply-templates mode="generate-id-from-path" select="parent::*"/><xsl:text>.</xsl:text><xsl:value-of select="concat('.',name(),'-',1+count(preceding-sibling::*[name()=name(current())]),'-')"/></xsl:template>

<!--MODE: GENERATE-ID-2 -->
<xsl:template match="/" mode="generate-id-2">U</xsl:template><xsl:template match="*" mode="generate-id-2" priority="2"><xsl:text>U</xsl:text><xsl:number count="*" level="multiple"/></xsl:template><xsl:template match="node()" mode="generate-id-2"><xsl:text>U.</xsl:text><xsl:number count="*" level="multiple"/><xsl:text>n</xsl:text><xsl:number count="node()"/></xsl:template><xsl:template match="@*" mode="generate-id-2"><xsl:text>U.</xsl:text><xsl:number count="*" level="multiple"/><xsl:text>_</xsl:text><xsl:value-of select="string-length(local-name(.))"/><xsl:text>_</xsl:text><xsl:value-of select="translate(name(),':','.')"/></xsl:template><!--Strip characters--><xsl:template match="text()" priority="-1"/>

<!--SCHEMA SETUP-->
<xsl:template match="/"><svrl:schematron-output xmlns:svrl="http://purl.oclc.org/dsdl/svrl" schemaVersion="" title="Demo Patient Record rules"><xsl:comment><xsl:value-of select="$archiveDirParameter"/>   
		 <xsl:value-of select="$archiveNameParameter"/>  
		 <xsl:value-of select="$fileNameParameter"/>  
		 <xsl:value-of select="$fileDirParameter"/></xsl:comment><svrl:active-pattern><xsl:attribute name="document"><xsl:value-of select="document-uri(/)"/></xsl:attribute><xsl:apply-templates/></svrl:active-pattern><xsl:apply-templates mode="M1" select="/"/></svrl:schematron-output></xsl:template>

<!--SCHEMATRON PATTERNS-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">Demo Patient Record rules</svrl:text>

<!--PATTERN -->


	<!--RULE -->
<xsl:template match="/PatientRecord/Patient" mode="M1" priority="1000"><svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="/PatientRecord/Patient"/>

		<!--ASSERT -->
<xsl:choose><xsl:when test="matches(BirthTime, '^[0-9]{8}$')"/><xsl:otherwise><svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="matches(BirthTime, '^[0-9]{8}$')"><xsl:attribute name="location"><xsl:apply-templates mode="schematron-select-full-path" select="."/></xsl:attribute><svrl:text>BirthTime must be formatted as YYYYMMDD.</svrl:text></svrl:failed-assert></xsl:otherwise></xsl:choose>

		<!--ASSERT -->
<xsl:choose><xsl:when test="Gender = 'M' or Gender = 'F' or Gender = 'O'"/><xsl:otherwise><svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="Gender = 'M' or Gender = 'F' or Gender = 'O'"><xsl:attribute name="location"><xsl:apply-templates mode="schematron-select-full-path" select="."/></xsl:attribute><svrl:text>Gender must be one of M, F or O.</svrl:text></svrl:failed-assert></xsl:otherwise></xsl:choose>

		<!--ASSERT -->
<xsl:choose><xsl:when test="string-length(normalize-space(Name)) &gt; 0"/><xsl:otherwise><svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="string-length(normalize-space(Name)) &gt; 0"><xsl:attribute name="location"><xsl:apply-templates mode="schematron-select-full-path" select="."/></xsl:attribute><svrl:text>Patient Name must not be empty.</svrl:text></svrl:failed-assert></xsl:otherwise></xsl:choose><xsl:apply-templates mode="M1" select="*|comment()|processing-instruction()"/></xsl:template><xsl:template match="text()" mode="M1" priority="-1"/><xsl:template match="@*|node()" mode="M1" priority="-2"><xsl:apply-templates mode="M1" select="*|comment()|processing-instruction()"/></xsl:template></xsl:stylesheet>