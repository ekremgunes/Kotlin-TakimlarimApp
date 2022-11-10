//
//
//
// THİS PAGE FOR GİTHUB , NOT PROJECT PAGE OR FİLE -> project -> futboltakimlarim.zip
//
//
package com.gunesekrem.denemeapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_takim_ekle.*
import java.io.ByteArrayOutputStream

class takim_ekleFragment : Fragment() {

    var secilen_gorsel : Uri? = null
    var secilen_bitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_takim_ekle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        takim_ekle_btn.setOnClickListener {
            takim_ekle(it)
        }
        takim_ekle_imageView.setOnClickListener {
            select_img(it)
        }

        super.onViewCreated(view, savedInstanceState)

    }
    fun takim_ekle(view: View){

        if (secilen_bitmap != null){

            val takim_adi = takim_Adi.text.toString()

            val kucukBitmap = kucukBitmapOlustur(secilen_bitmap!!,300)//boyut uygulamaya göre ayarlanmalı 200-600 arası hatta 700 bile denenir

            val outputStream = ByteArrayOutputStream()
            kucukBitmap!!.compress(Bitmap.CompressFormat.JPEG,100,outputStream)
            val bytedizisi = outputStream.toByteArray()

            try {
                context?.let {
                    val database = it.openOrCreateDatabase("TakimlarDb", Context.MODE_PRIVATE,null)

                    database.execSQL("CREATE TABLE IF NOT EXISTS Takimlar (id INTEGER PRIMARY KEY,takim_adi VARCHAR , takim_img BLOB)")

                    val sqlString = "INSERT INTO Takimlar (takim_adi,takim_img) VALUES (?,?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1,takim_adi)
                    statement.bindBlob(2,bytedizisi)
                    statement.execute()
                    //statement.close()

                    Toast.makeText(it,"${takim_adi} takimlara eklendi",Toast.LENGTH_LONG).show()

                }

            }catch (e: Exception){
                println(e.message)
                context?.let {
                    Toast.makeText(it,"Bir hata oluştu :(",Toast.LENGTH_LONG).show()
                }
            }
            val action = takim_ekleFragmentDirections.actionTakimEkleFragmentToTakimListFragment()
            Navigation.findNavController(view).navigate(action)

        }else{
            context?.let {
                Toast.makeText(it," Resim Ekleyiniz ! ",Toast.LENGTH_LONG).show()
            }
        }
    }

    fun select_img(view: View){
        //izin nasıl sorulur kontrol edilir
        //permision granted izin verildi denied verilmedi demk
        activity?.let {
            if(ContextCompat.checkSelfPermission(it!!.applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin verilmemiş izin iste
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)

            }else{
                //izin zaten verilmiş galeriye git
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //izin zaten verilmiş galeriye git
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                //registerForActivityResult(galeriIntent,2)
                startActivityForResult(galeriIntent,2)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
            //secilen resmin uri ı yani pathi nerede oldugunu alıyoruz data.data ile
            secilen_gorsel = data.data
            try {
                context?.let {
                    if (secilen_gorsel != null ){
                        if ( Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(it.contentResolver,secilen_gorsel!! )
                            secilen_bitmap = ImageDecoder.decodeBitmap(source)
                            takim_ekle_imageView.setImageBitmap(secilen_bitmap)

                        }else{
                            secilen_bitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,secilen_gorsel)
                            takim_ekle_imageView.setImageBitmap(secilen_bitmap)
                        }
                    }
                }



            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        else{
            context?.let {
                Toast.makeText(it,"Bir hata oluştu resme ulaşamadık :(",Toast.LENGTH_LONG).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)

    }

    fun kucukBitmapOlustur(kullanicininBitmap : Bitmap , MaximumBoyut :Int): Bitmap? {

        var width = kullanicininBitmap.width
        var height = kullanicininBitmap.height
        val bitmapOran = width.toDouble()  / height.toDouble()

        if(bitmapOran > 1 ){
            //görsel yataydır
            width = MaximumBoyut;
            var kisaltilmisHeight = width / bitmapOran
            height = kisaltilmisHeight.toInt()


        }else{
            height = MaximumBoyut;
            var kisaltilmisWidth = height * bitmapOran
            width = kisaltilmisWidth.toInt()

        }

        return Bitmap.createScaledBitmap(kullanicininBitmap,width,height,true)


    }
}
