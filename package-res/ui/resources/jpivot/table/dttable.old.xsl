<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="xtable-component">
		<table cellspacing="0" cellpadding="2" id="drillthrough-table">
			<xsl:apply-templates />
		</table>
	</xsl:template>

	<xsl:template match="xtr">
		<tr>
			<xsl:apply-templates />
		</tr>
	</xsl:template>

	<xsl:template match="xth">
		<th>
			<xsl:value-of select="."/>
		</th>
	</xsl:template>

	<xsl:template match="xtd">
		<td>
			<xsl:value-of select="."/> 
		</td>
	</xsl:template>
</xsl:stylesheet>