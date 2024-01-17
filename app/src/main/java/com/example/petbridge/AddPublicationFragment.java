package com.example.petbridge;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;


import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.petbridge.firebase.FirebaseManager;
import com.example.petbridge.navigation.HomeFragment;
import com.example.petbridge.navigation.Publication;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddPublicationFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;
    private String mParam1;
    private String mParam2;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ImageView addImage;
    private ImageView imagepub;
    private EditText pubText;
    private Button post;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Uri selectedImageUri;

    public AddPublicationFragment() {
        // Required empty public constructor

    }

    public void registerImage() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            selectedImageUri = data.getData();
                            imagepub.setImageURI(selectedImageUri);
                        }
                    }
                }
        );
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        pickImageLauncher.launch(galleryIntent);
    }

    public static AddPublicationFragment newInstance(String param1, String param2) {
        AddPublicationFragment fragment = new AddPublicationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        db = FirebaseManager.getFirestoreInstance();
        auth = FirebaseAuth.getInstance();
        registerImage();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_publication, container, false);
        addImage = view.findViewById(R.id.addImage);
        imagepub = view.findViewById(R.id.imagepub);
        post = view.findViewById(R.id.post);
        pubText = view.findViewById(R.id.publicationText);

        Uri defaultImageUri = Uri.parse("android.resource://com.example.petbridge/drawable/help");
        selectedImageUri = (selectedImageUri != null) ? selectedImageUri : defaultImageUri;

        addImage.setOnClickListener(v -> choseOption());

        post.setOnClickListener(v -> {
            if (controlText(pubText.getText().toString())) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                StorageReference imageReference = storageReference.child("pictures/" + selectedImageUri);
                imageReference.putFile(selectedImageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            imageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                CollectionReference publicationsRef = db.collection("publications");
                                String userId = auth.getCurrentUser().getUid();
                                DocumentReference newPublicationRef = publicationsRef.document();
                                Publication newPublication = new Publication(
                                        userId,
                                        imageUrl,
                                        pubText.getText().toString()
                                );
                                newPublicationRef.set(newPublication)
                                        .addOnSuccessListener(aVoid -> {
                                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                            fragmentManager.beginTransaction()
                                                    .replace(R.id.fragment_container, new HomeFragment())
                                                    .addToBackStack(null)
                                                    .commit();
                                        })
                                        .addOnFailureListener(e -> {
                                        });
                            });
                        })
                        .addOnFailureListener(e -> {
                        });
            } else {
            }
        });
        return view;
    }


    private boolean controlText(String text) {
        return text.length() > 3;
    }

    private void choseOption() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose an option");
        String[] options = {"Take Photo", "Choose from Gallery"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    checkCameraPermission();
                    break;
                case 1:
                    openGallery();
                    break;
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            selectedImageUri = bitmapToUri(bitmap);
            imagepub.setImageURI(selectedImageUri);

        }
    }

    public Uri bitmapToUri(Bitmap mBitmap) {
        Uri uri = null;
        try {
            File file = createImageFile();
            FileOutputStream out = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            uri = Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    public void TakePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 100);

    }
    public void checkCameraPermission(){
        if(ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity() , new String[]{
                    Manifest.permission.CAMERA},100);
        }else {
            TakePicture();
        }

    }
}