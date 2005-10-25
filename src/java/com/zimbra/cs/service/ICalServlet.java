/*
 * ***** BEGIN LICENSE BLOCK *****
 * Version: ZPL 1.1
 * 
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.1 ("License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.zimbra.com/license
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 * 
 * The Original Code is: Zimbra Collaboration Suite.
 * 
 * The Initial Developer of the Original Code is Zimbra, Inc.
 * Portions created by Zimbra are Copyright (C) 2005 Zimbra, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): 
 * 
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.cs.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.mail.internet.MailDateFormat;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ValidationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Appointment;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Mailbox.OperationContext;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mailbox.calendar.InviteInfo;
import com.zimbra.cs.service.mail.CalendarUtils;
import com.zimbra.soap.Element;

/**
 * simple iCal servlet on a mailbox. URL is:
 * 
 *  http://server/service/ical/cal.ics[?...support-range-at-some-point...]
 *  
 *  need to support a range query at some point, right now get -30 thorugh +90 days from today
 *
 */

public class ICalServlet extends ZimbraBasicAuthServlet {

    private static final String  WWW_AUTHENTICATE_HEADER = "WWW-Authenticate";
    private static final String  WWW_AUTHENTICATE_VALUE = "BASIC realm=\"Zimbra iCal\"";
    
    private static final String PARAM_QUERY = "query";

    private static Log mLog = LogFactory.getLog(ICalServlet.class);
    
    private static final long MSECS_PER_DAY = 1000*60*60*24;

    public void doAuthGet(HttpServletRequest req, HttpServletResponse resp, Account acct, Mailbox mailbox)
    throws ServiceException, IOException
    {
        String pathInfo = req.getPathInfo().toLowerCase();
        boolean isRss = pathInfo != null && pathInfo.endsWith("rss");

        if (isRss) {
            long start = System.currentTimeMillis() - (7*MSECS_PER_DAY);
            long end = start+(14*MSECS_PER_DAY);            
            doRss(req, resp, acct, mailbox, start, end);
        } else {
            long start = System.currentTimeMillis() - (30*MSECS_PER_DAY);
            long end = start+(90*MSECS_PER_DAY);
            doIcal(req, resp, acct, mailbox, start, end);            
        }
    }

    private void doIcal(HttpServletRequest req, HttpServletResponse resp, Account acct, Mailbox mailbox, long start, long end)
    throws ServiceException, IOException
    {
        resp.setContentType("text/calendar");

        try {
            Calendar cal = mailbox.getCalendarForRange(null, start, end);
            
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            CalendarOutputter calOut = new CalendarOutputter();
            calOut.output(cal, buf);            
            resp.getOutputStream().write(buf.toByteArray());
        } catch (ValidationException e) {
            throw ServiceException.FAILURE("For account:"+acct.getName()+" mbox:"+mailbox.getId()+" unable to get calendar "+e, e);
        }
    }

    private void doRss(HttpServletRequest req, HttpServletResponse resp, Account acct, Mailbox mailbox, long start, long end)
    throws ServiceException, IOException
    {
        resp.setContentType("application/rss+xml");
            
        StringBuffer sb = new StringBuffer();

        sb.append("<?xml version=\"1.0\"?>");
            
        Element.XMLElement rss = new Element.XMLElement("rss");
        rss.addAttribute("version", "2.0");

        Element channel = rss.addElement("channel");
        channel.addElement("title").setText("Zimbra Mail: "+acct.getName());
            
        channel.addElement("generator").setText("Zimbra RSS Feed Servlet");

        OperationContext octxt = new OperationContext(acct);            
        Collection appts = mailbox.getAppointmentsForRange(octxt, start, end, Mailbox.ID_FOLDER_CALENDAR);
                
            //channel.addElement("description").setText(query);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        MailDateFormat mdf = new MailDateFormat();
        for (Iterator apptIt = appts.iterator(); apptIt.hasNext(); ) {            
            Appointment appt = (Appointment) apptIt.next();

            Collection instances = appt.expandInstances(start, end);
            for (Iterator instIt = instances.iterator(); instIt.hasNext(); ) {
                Appointment.Instance inst = (Appointment.Instance) instIt.next();
                InviteInfo invId = inst.getInviteInfo();
                Invite inv = appt.getInvite(invId.getMsgId(), invId.getComponentId());
                Element item = channel.addElement("item");
                item.addElement("title").setText(inv.getName());
                item.addElement("pubDate").setText(sdf.format(new Date(inst.getStart())));
                /*                
                StringBuffer desc = new StringBuffer();
                sb.append("Start: ").append(sdf.format(new Date(inst.getStart()))).append("\n");
                sb.append("End: ").append(sdf.format(new Date(inst.getEnd()))).append("\n");
                sb.append("Location: ").append(inv.getLocation()).append("\n");
                sb.append("Notes: ").append(inv.getFragment()).append("\n");
                item.addElement("description").setText(sb.toString());
                */
                item.addElement("description").setText(inv.getFragment());
                item.addElement("author").setText(CalendarUtils.paramVal(inv.getOrganizer(), Parameter.CN));
                /* TODO: guid, links, etc */
                //Element guid = item.addElement("guid");
                //guid.setText(appt.getUid()+"-"+inv.getStartTime().getUtcTime());
                //guid.addAttribute("isPermaLink", "false");
            }                    
        }
        sb.append(rss.toString());
        resp.getOutputStream().write(sb.toString().getBytes());
    }
}
