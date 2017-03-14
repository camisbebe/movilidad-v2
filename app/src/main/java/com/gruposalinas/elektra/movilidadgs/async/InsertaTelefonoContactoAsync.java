package com.gruposalinas.elektra.movilidadgs.async;

import android.os.AsyncTask;

import com.gruposalinas.elektra.movilidadgs.Activities.Contacto;
import com.gruposalinas.elektra.movilidadgs.Activities.Contactos;
import com.gruposalinas.elektra.movilidadgs.beans.Multimedia;
import com.gruposalinas.elektra.movilidadgs.geolocation.ServicePanico;
import com.gruposalinas.elektra.movilidadgs.geolocation.Service_contact;
import com.gruposalinas.elektra.movilidadgs.webservices.InsertaTelefonoContactoWS;

/**
 * Created by yvegav on 17/10/2016.
 */
public class InsertaTelefonoContactoAsync extends AsyncTask<Multimedia,Void,Multimedia> {
   Contactos contacto;
    ServicePanico servicePanico;
    Service_contact service_contact;

    public InsertaTelefonoContactoAsync(ServicePanico servicePanico) {
        this.servicePanico = servicePanico;
    }

    public InsertaTelefonoContactoAsync(Contactos contacto) {
        this.contacto = contacto;
    }

    public InsertaTelefonoContactoAsync(Service_contact service_contact) {
        this.service_contact = service_contact;
    }

    @Override
    protected Multimedia doInBackground(Multimedia... params)
    {
        InsertaTelefonoContactoWS insertaTelefonoContactoWS= new InsertaTelefonoContactoWS();
        Multimedia multimedia= insertaTelefonoContactoWS.contactos(params[0]);
        return multimedia;
    }

    @Override
    protected void onPostExecute(Multimedia multimedia)
    {

        if (service_contact!=null)
        {

            service_contact.res(multimedia);
        }
    }
}
