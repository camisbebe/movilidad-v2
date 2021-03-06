package com.gruposalinas.elektra.movilidadgs.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gruposalinas.elektra.movilidadgs.R;

/**
 * Created by Adrian Guzman on 13/04/2016.
 */
public class AdapterUbicaciones_Zonas extends ArrayAdapter<String>
{
    Context context;
    String nombres_Ubicaciones[];
    int  imagen [];

    public void setImagen(int[] imagen) {
        this.imagen = imagen;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setNombres_Ubicaciones(String[] nombres_Ubicaciones) {
        this.nombres_Ubicaciones = nombres_Ubicaciones;
    }

    public AdapterUbicaciones_Zonas(Context context, int resource,String string[])
    {
        super(context, resource,string);

        this.context=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vista = inflater.inflate(R.layout.item_zonas_ubicaciones, parent, false);
        TextView textView=(TextView)vista.findViewById(R.id.ubicacion_zona);
        ImageView imageView=(ImageView)vista.findViewById(R.id.icono_ubicacion);
        LinearLayout linearLayout=(LinearLayout)vista.findViewById(R.id.contenedor_ubicacion);
        textView.setText(nombres_Ubicaciones[position]);
        imageView.setBackgroundResource(imagen[position]);
        boolean validar=esImpar(position);
        if(!validar)
        {


            linearLayout.setBackgroundColor(Color.WHITE);

        }

        return vista;
    }

    public boolean esImpar(int iNumero) {
        if (iNumero%2!=0)
            return true;
        else
            return false;
    }
}
