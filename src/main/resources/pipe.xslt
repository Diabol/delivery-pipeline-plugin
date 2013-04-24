<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="view">
        <html>
            <head>
                <link rel="stylesheet" type="text/css" href="pipe.css"/>
            </head>
            <body>
                <xsl:apply-templates select="pipe"/>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="pipe">
        <section class="pipe">
            <h1><xsl:value-of select="@name"/></h1>
            <xsl:apply-templates select="stage"/>
        </section>
    </xsl:template>

    <xsl:template match="stage">
        <section class="stage">
            <h1><xsl:value-of select="@name"/></h1>
            <ul>
                <xsl:apply-templates select="task"/>
            </ul>
        </section>
    </xsl:template>

    <xsl:template match="task">
        <xsl:element name="li">
            <xsl:attribute name="class">
                <xsl:value-of select="@status"/>
            </xsl:attribute>
            <xsl:value-of select="@name"/>
        </xsl:element>
    </xsl:template>

</xsl:transform>