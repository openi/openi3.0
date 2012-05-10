<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Edited by XMLSpy® -->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<table border="1" cellspacing="0" cellpadding="2" id="drillthrough-table">
			<thead>
				<tr>
					<xsl:for-each select="xtable-component/xtr/xth">
						<th align="left">
							<xsl:value-of select="." />
						</th>
					</xsl:for-each>
				</tr>
			</thead>
			<tbody>
				<xsl:for-each select="xtable-component/xtr">
					<tr>
						<xsl:for-each select="xtd">
							<td align="left">
								<xsl:value-of select="." />
							</td>
						</xsl:for-each>
					</tr>
				</xsl:for-each>
			</tbody>
		</table>
	</xsl:template>
</xsl:stylesheet>
