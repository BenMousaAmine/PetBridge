package com.example.petbridge.navigation;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.petbridge.R;
import com.example.petbridge.auth.LoginActivity;
import com.example.petbridge.firebase.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ImageView profileImage;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Uri selectedImageUri;

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
            // Handle arguments if any
        }
        registerImage();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseManager.getFirestoreInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profileImage = view.findViewById(R.id.profileImageProfileAc);
        TextView nome = view.findViewById(R.id.nameProfileAc);
        ImageView addimg = view.findViewById(R.id.changeImageProfileAc);

        addimg.setOnClickListener(v -> addImageProfile());

        Button logoutButton = view.findViewById(R.id.logoutProfileAc);
        TextView deleteAccountButton = view.findViewById(R.id.deleteAccountAc);

        String userId = mAuth.getCurrentUser().getUid();

        loadUserProfileDetails(userId, profileImage, nome);

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });

        deleteAccountButton.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                user.delete()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                            } else {
                                Exception exception = task.getException();
                                if (exception != null) {
                                    Toast.makeText(getActivity(), "Error deleting account: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        return view;
    }

    private void addImageProfile() {
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

    public void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    Manifest.permission.CAMERA}, 100);
        } else {
            TakePicture();
        }
    }

    public void TakePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            selectedImageUri = bitmapToUri(bitmap);
            profileImage.setImageURI(selectedImageUri);
            uploadProfileImageToStorage(selectedImageUri);
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    public void registerImage() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            selectedImageUri = data.getData();
                            profileImage.setImageURI(selectedImageUri);
                            uploadProfileImageToStorage(selectedImageUri);
                        }
                    }
                }
        );
    }

    private void uploadProfileImageToStorage(Uri newImageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("profile_images/" + mAuth.getCurrentUser().getUid() + "_profile.jpg");

        imageRef.putFile(newImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateProfileImageInFirestore(mAuth.getCurrentUser().getUid(), uri.toString(), profileImage, null);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Image Upload Failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfileImageInFirestore(String userId, String imageUrl, ImageView profileImage, TextView nome) {
        db.collection("Users")
                .document(userId)
                .update("Image", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    if (nome != null) {
                        // If you have a TextView for the name, you can update it here
                    }
                    Picasso.get().load(Uri.parse(imageUrl)).into(profileImage);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to update image URL in Firestore", Toast.LENGTH_SHORT).show();
                });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        galleryIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pickImageLauncher.launch(galleryIntent);
    }

    private void loadUserProfileDetails(String userId, ImageView profileImage, TextView nome) {
        db.collection("Users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String name = document.getString("Name");
                            String lastName = document.getString("LastName");
                            String image = document.getString("Image");
                            if (nome != null) {
                                nome.setText(name + " " + lastName);
                            }
                            Picasso.get().load(Uri.parse(image)).into(profileImage);
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception != null) {
                            Toast.makeText(getActivity(), "Error reading data: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
