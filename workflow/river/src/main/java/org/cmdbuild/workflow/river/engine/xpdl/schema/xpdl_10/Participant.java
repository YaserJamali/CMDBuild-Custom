//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.04.02 at 07:43:50 AM CEST 
//
package org.cmdbuild.workflow.river.engine.xpdl.schema.xpdl_10;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>
 * Java class for anonymous complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}ParticipantType"/>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}Description" minOccurs="0"/>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}ExternalReference" minOccurs="0"/>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}ExtendedAttributes" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Id" use="required" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="Name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "participantType",
    "description",
    "externalReference",
    "extendedAttributes"
})
@XmlRootElement(name = "Participant")
public class Participant {

    @XmlElement(name = "ParticipantType", required = true)
    protected ParticipantType participantType;
    @XmlElement(name = "Description")
    protected String description;
    @XmlElement(name = "ExternalReference")
    protected ExternalReference externalReference;
    @XmlElement(name = "ExtendedAttributes")
    protected ExtendedAttributes extendedAttributes;
    @XmlAttribute(name = "Id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String id;
    @XmlAttribute(name = "Name")
    protected String name;

    /**
     * Gets the value of the participantType property.
     *
     * @return possible object is {@link ParticipantType }
     *
     */
    public ParticipantType getParticipantType() {
        return participantType;
    }

    /**
     * Sets the value of the participantType property.
     *
     * @param value allowed object is {@link ParticipantType }
     *
     */
    public void setParticipantType(ParticipantType value) {
        this.participantType = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the externalReference property.
     *
     * @return possible object is {@link ExternalReference }
     *
     */
    public ExternalReference getExternalReference() {
        return externalReference;
    }

    /**
     * Sets the value of the externalReference property.
     *
     * @param value allowed object is {@link ExternalReference }
     *
     */
    public void setExternalReference(ExternalReference value) {
        this.externalReference = value;
    }

    /**
     * Gets the value of the extendedAttributes property.
     *
     * @return possible object is {@link ExtendedAttributes }
     *
     */
    public ExtendedAttributes getExtendedAttributes() {
        return extendedAttributes;
    }

    /**
     * Sets the value of the extendedAttributes property.
     *
     * @param value allowed object is {@link ExtendedAttributes }
     *
     */
    public void setExtendedAttributes(ExtendedAttributes value) {
        this.extendedAttributes = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

}
