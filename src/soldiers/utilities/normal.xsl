<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="xml" encoding="UTF-8" indent="no"/>

<xsl:template match="/|*|@*|comment()|processing-instruction()">
  <xsl:copy>
    <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="text()">
	<xsl:copy-of select="normalize-space(.)"/>
</xsl:template>

</xsl:stylesheet>
