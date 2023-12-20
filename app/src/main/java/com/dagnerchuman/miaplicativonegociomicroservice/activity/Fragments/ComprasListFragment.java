package com.dagnerchuman.miaplicativonegociomicroservice.activity.Fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.RecyclerView;

import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.dagnerchuman.miaplicativonegociomicroservice.adapter.CompraAdapter;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceCompras;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ConfigApi;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Compra;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import cn.pedant.SweetAlert.SweetAlertDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ComprasListFragment extends Fragment implements CompraAdapter.BoletaDownloadListener {

    private ImageButton btnBackToEntrada;
    private ApiServiceCompras apiServiceCompras;
    private RecyclerView recyclerView;
    private CompraAdapter comprasAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listado_de_compras, container, false);

        // Inicializa las vistas
        btnBackToEntrada = view.findViewById(R.id.btnBackToEntrada);
        recyclerView = view.findViewById(R.id.recyclerViewCompras);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        comprasAdapter = new CompraAdapter(this);
        recyclerView.setAdapter(comprasAdapter);

        // Inicializa apiServiceCompras aquí
        apiServiceCompras = ConfigApi.getInstanceCompra(requireContext());

        // Realiza la solicitud para obtener la lista de compras
        obtenerListaDeCompras();

        btnBackToEntrada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Maneja el clic del botón en tu EntradaActivity
                // Puedes abrir la actividad correspondiente desde aquí
            }
        });

        return view;
    }

    private void obtenerListaDeCompras() {


        Call<List<Compra>> call = apiServiceCompras.getAllComprasOfUser();

        call.enqueue(new Callback<List<Compra>>() {
            @Override
            public void onResponse(@NonNull Call<List<Compra>> call, @NonNull Response<List<Compra>> response) {
                if (response.isSuccessful()) {
                    List<Compra> todasLasCompras = response.body();

                    if (todasLasCompras != null) {
                        // Filtra las compras por userId si es necesario
                        List<Compra> comprasDelUsuario = todasLasCompras;

                        if (!comprasDelUsuario.isEmpty()) {
                            // Actualiza el adaptador con la lista de compras del usuario
                            comprasAdapter.setCompras(comprasDelUsuario);
                        } else {
                            // Maneja el caso en que el usuario no tenga compras
                            mostrarMensaje("No se encontraron compras para este usuario");
                        }
                    } else {
                        // Maneja el caso en que no haya compras
                        mostrarMensaje("No se encontraron compras");
                    }
                } else {
                    // La solicitud no fue exitosa
                    int statusCode = response.code();
                    if (statusCode == 403) {
                        // Maneja el caso de autorización denegada
                        mostrarMensaje("Acceso denegado. Comprueba tu autorización.");
                    } else {
                        // Resto del manejo de errores
                        mostrarMensaje("Error al obtener las compras. Código de estado HTTP: " + statusCode);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Compra>> call, @NonNull Throwable t) {
                // Maneja el error de la solicitud de red aquí
                mostrarMensaje("Error en la solicitud: " + t.getMessage());
            }
        });
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBoletaDownload(Compra compra) {
        // Verifica que el estado de la compra sea "Pago Completado" antes de permitir la descarga
        if (compra.getEstadoCompra().equals("Pago Completado")) {
            Long compraId = compra.getId();

            // Obtiene el nombre del usuario de SharedPreferences
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserDataUser", Context.MODE_PRIVATE);
            String userName = sharedPreferences.getString("userName", "");

            // Configuración del tamaño del documento (ajustado para contenido en una sola hoja)
            PageSize pageSize = new PageSize(80f, 100f);

            // Directorio donde se almacenarán los archivos PDF descargados
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            Log.d("MiApp", "Directorio de descargas: " + directory); // Agregar log

            String fileName = "boleta_" + userName + "_" + compraId + ".pdf";
            String filePath = directory + File.separator + fileName;
            Log.d("MiApp", "Ruta del archivo PDF: " + filePath); // Agregar log

            try {
                // Inicializa el documento PDF con iText
                PdfWriter pdfWriter = new PdfWriter(filePath);
                PdfDocument pdfDocument = new PdfDocument(pdfWriter);

                // Utiliza el PageSize al agregar el Document al PdfDocument
                Document document = new Document(pdfDocument, pageSize);

                // Configura la alineación justificada para el texto
                document.setTextAlignment(TextAlignment.JUSTIFIED);

                // Ajusta el tamaño de la fuente y los márgenes
                float fontSize = 3f;  // Tamaño de fuente aún más pequeño
                document.setFontSize(fontSize);
                document.setMargins(1, 1, 1, 1);  // Márgenes aún más pequeños

                // Agrega contenido al documento con alineación justificada
                document.add(new Paragraph("Detalles de la Compra: " + compra.getTitulo()));
                document.add(new Paragraph("Fecha de Compra: " + compra.getFechaCompra()));
                document.add(new Paragraph("Precio de Compra: " + compra.getPrecioCompra()));

                // Agrega más detalles de la compra según tus necesidades

                // Cierra el documento
                document.close();
                Log.d("MiApp", "PDF generado correctamente en: " + filePath); // Agregar log

                // Notifica al usuario y abre el PDF descargado
                showSuccessSweetAlert("Boleta descargada", "La boleta se ha descargado correctamente", filePath);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("MiApp", "Error al generar el PDF: " + e.getMessage());
                showErrorSweetAlert("Error al generar la boleta", e.getMessage());
            }
        } else {
            // Si el estado no es "Pago Completado", muestra un Sweet Alert indicando que no se puede descargar la boleta con el estado actual de la compra
            showErrorSweetAlert("No se puede descargar la boleta", "Estado de compra: " + compra.getEstadoCompra());
        }
    }

    private void showSuccessSweetAlert(String title, String content, String filePath) {
        new SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(title)
                .setContentText(content)
                .setConfirmText("OK")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        openPdfFile(filePath);
                    }
                })
                .show();
    }


    private void showErrorSweetAlert(String title, String content) {
        new SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                .setTitleText(title)
                .setContentText(content)
                .setConfirmText("OK")
                .show();
    }
    private void openPdfFile(String filePath) {
        // Abre el archivo PDF utilizando una aplicación de visor de PDF instalada
        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Maneja el caso en que no haya una aplicación de visor de PDF instalada
            e.printStackTrace();
            Toast.makeText(requireContext(), "No se encontró una aplicación para abrir el PDF.", Toast.LENGTH_SHORT).show();
        }
    }

}
