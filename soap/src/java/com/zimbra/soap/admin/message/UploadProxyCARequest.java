/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2012, 2013, 2014, 2016 Synacor, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.CertMgrConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Upload proxy CA
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=CertMgrConstants.E_UPLOAD_PROXYCA_REQUEST)
public class UploadProxyCARequest {

    /**
     * @zm-api-field-description Certificate attach ID
     */
    @XmlAttribute(name=CertMgrConstants.A_CERT_AID /* cert.aid */, required=true)
    private String certificateAttachId;

    /**
     * @zm-api-field-description Certificate name
     */
    @XmlAttribute(name=CertMgrConstants.A_CERT_NAME /* cert.filename */, required=true)
    private String certificateName;

    private UploadProxyCARequest() {
    }

    private UploadProxyCARequest(String certificateAttachId, String certificateName) {
        setCertificateAttachId(certificateAttachId);
        setCertificateName(certificateName);
    }

    public static UploadProxyCARequest createForAttachIdAndCert(String certificateAttachId, String certificateName) {
        return new UploadProxyCARequest(certificateAttachId, certificateName);
    }

    public void setCertificateAttachId(String certificateAttachId) { this.certificateAttachId = certificateAttachId; }
    public void setCertificateName(String certificateName) { this.certificateName = certificateName; }
    public String getCertificateAttachId() { return certificateAttachId; }
    public String getCertificateName() { return certificateName; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("certificateAttachId", certificateAttachId)
            .add("certificateName", certificateName);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
