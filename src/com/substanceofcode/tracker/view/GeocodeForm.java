/*
 * GeocodeForm.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package com.substanceofcode.tracker.view;

import com.substanceofcode.geocode.GeocodeManager;
import com.substanceofcode.geocode.GeocodeRequest;
import com.substanceofcode.geocode.GeocodeStatusCallback;
import com.substanceofcode.geocode.PlaceDescription;
import com.substanceofcode.localization.LocaleManager;
import com.substanceofcode.tracker.controller.Controller;
import com.substanceofcode.tracker.model.Place;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

/**
 *
 * @author kaspar
 */
public class GeocodeForm extends Form implements CommandListener, GeocodeStatusCallback
{
    private Controller controller = null;
    
    private TextField searchField = null;
    private ChoiceGroup geocodeProviders = null;
    private Command okCommand = null;
    private Command cancelCommand = null;
    
    private GeocodeRequest currentRequest = null;
    
    //waiting-dialog
    private Form waitingDialog = null;
    private Command waitingButton = null;
    
    //error-dialog
    private Form errorDialog = null;
    private StringItem errorMsg = null;
    private Command errorButton = null;
    
    public GeocodeForm(Controller c)
    {
        super(LocaleManager.getMessage("geocode_form_title"));
        
        controller = c;
        
        //build form
        searchField = new TextField(LocaleManager.getMessage("geocode_form_searchField_label"), "", 32, TextField.ANY);
        append(searchField);
        
        
        String[] names = GeocodeManager.getManager().providerLabels();
        geocodeProviders = new ChoiceGroup(LocaleManager.getMessage("geocode_form_services"), ChoiceGroup.EXCLUSIVE, names, null);
        geocodeProviders.setSelectedIndex(GeocodeManager.getManager().getSelectedIndex(), true);
        append(geocodeProviders);
        
        
        okCommand = new Command(LocaleManager.getMessage("geocode_form_command_search"), Command.SCREEN, 1);
        this.addCommand(okCommand);

        cancelCommand = new Command(LocaleManager.getMessage("menu_cancel"), Command.SCREEN, 2);
        this.addCommand(cancelCommand);
        
        this.setCommandListener(this);
        
        
        waitingDialog = new Form(LocaleManager.getMessage("geocode_form_progress_dialog_title"));
        waitingDialog.append(new StringItem("", LocaleManager.getMessage("geocode_form_sending_request")));
        waitingButton = new Command(LocaleManager.getMessage("geocode_form_command_abort"), Command.SCREEN, 1);
        waitingDialog.addCommand(waitingButton);
        waitingDialog.setCommandListener(this);
        
        errorDialog = new Form(LocaleManager.getMessage("geocode_form_progress_dialog_title"));
        errorMsg = new StringItem("", "error"); //this text will be overwritten
        errorDialog.append(errorMsg);
        errorButton = new Command(LocaleManager.getMessage("menu_ok"), Command.SCREEN, 1);
        errorDialog.addCommand(errorButton);
        errorDialog.setCommandListener(this);
        
    }

    public void commandAction(Command cmd, Displayable displayable) {
        
        if(cmd == okCommand)
        {
            GeocodeManager geomanager = GeocodeManager.getManager();
            geomanager.setCurrentProvider(geocodeProviders.getSelectedIndex());
            
            PlaceDescription descr = new PlaceDescription(searchField.getString(), "");
            currentRequest = geomanager.getCurrentProvider().geocodePlace(descr, this);
        }
        
        if(cmd == cancelCommand)
        {
            controller.showTrail();
        }
        
        //errorDialog
        if(cmd == errorButton)
        {
            controller.getDisplay().setCurrent(this);
        }
        
        //progressdialog
        if(cmd == waitingButton)
        {
            currentRequest.cancel();
        }
    }

    public void geocodeRequestStatusDidChange(GeocodeRequest request, int status) {
        //searchField.setLabel(request.getStatusString());
        switch (status) {
            case (GeocodeRequest.STATUS_REQUESTING):
                controller.getDisplay().setCurrent(waitingDialog);
                break;
                
            case (GeocodeRequest.STATUS_ERROR):
                errorMsg.setText(LocaleManager.getMessage("geocode_form_service_error"));
                controller.getDisplay().setCurrent(errorDialog);
                break;
                
            case (GeocodeRequest.STATUS_NOT_FOUND):
                errorMsg.setText(LocaleManager.getMessage("geocode_form_service_place_not_found"));
                controller.getDisplay().setCurrent(errorDialog);
                break;
                
            case (GeocodeRequest.STATUS_FOUND):
                controller.addPlace(new Place(request.getPlaceDescription().getPlaceLabel(), request.getLocation()));
                controller.getTrailCanvas().setMapCenter(request.getLocation());
                controller.showTrail();
                break;
        }

    }

}
