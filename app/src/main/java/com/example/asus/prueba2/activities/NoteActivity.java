package com.example.asus.prueba2.activities;

import android.content.DialogInterface;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.asus.prueba2.R;
import com.example.asus.prueba2.adapters.NoteAdapter;
import com.example.asus.prueba2.models.Board;
import com.example.asus.prueba2.models.Note;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;

public class NoteActivity extends AppCompatActivity implements RealmChangeListener<Board>, AdapterView.OnItemClickListener {

    private int usuarioSeleccionado = -1;
    private Object mActionMode;

    private ListView listView;
    private FloatingActionButton fab;

    private NoteAdapter adapter;
    private RealmList<Note> notes;

    private Realm realm;

    private int boardID;
    private Board board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        //BD
        realm = Realm.getDefaultInstance();

        // recuperar el id que nos llega
        if(getIntent().getExtras() != null)
            boardID = getIntent().getExtras().getInt("id");

        //** con el id recuperamos el tablero de la BD **/
        board = realm.where(Board.class).equalTo("id", boardID).findFirst();

        // ajuntamos el metodo de refresh
        board.addChangeListener(this);



        // del tablero anterior obtenemos todas las notas
        notes = board.getNotes();

        // le damos el titulo ala actividad con lo que trae el tablero con el atributo title
        this.setTitle(board.getTitle());


        // instanciamos el floatingActionButton, listWiew, adaptador
        fab = (FloatingActionButton) findViewById(R.id.fabAddNote);
        listView = (ListView) findViewById(R.id.listViewNote);
        adapter = new NoteAdapter(this, notes, R.layout.list_view_note_item);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                showAlertForCreatingNotes("Agregar notas", "Escribe una nota para" + board.getTitle() + ".");

            }
        });

        registerForContextMenu(listView);

    }

    /**    Dialogs **/
    private void showAlertForCreatingNotes(String title, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);


        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_note, null);
        builder.setView(viewInflated);


        final EditText input = (EditText) viewInflated.findViewById(R.id.editTextNewNote);


        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String note = input.getText().toString().trim();


                if(note.length() > 0)
                    createNewNote(note);
                else
                    Toast.makeText(getApplicationContext(), "The note can't be empty", Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void prueba( String title, final Note note) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        if (title != null) builder.setTitle(title);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.prueba, null);
        builder.setView(viewInflated);
        final AlertDialog dialog = builder.create();


        final Button btnActualizar = (Button) viewInflated.findViewById(R.id.btnActu);
        final Button btnEliminar = (Button) viewInflated.findViewById(R.id.btnEli);

        final String id = String.valueOf(note.getId());

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Actualiza" ,Toast.LENGTH_SHORT).show();
                showAlertForEditingNote("Actualizar", "Ingresa tu nueva nota", note);
                dialog.cancel();
                //showAlertForCreatingNotes("hoal","mesage");
            }
        });

        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Eliminada",Toast.LENGTH_SHORT).show();
                deleteNote(note);
                dialog.cancel();
            }
        });




        dialog.show();

    }


    private void showAlertForEditingNote(String title, String message, final Note note) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_note, null);
        builder.setView(viewInflated);

        final EditText input = (EditText) viewInflated.findViewById(R.id.editTextNewNote);
        input.setText(note.getDescription());

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String noteDescription = input.getText().toString().trim();

                if (noteDescription.length() == 0)
                    Toast.makeText(getApplicationContext(), "The name is required to edit the current note", Toast.LENGTH_LONG).show();
                else if (noteDescription.equals(note.getDescription()))
                    Toast.makeText(getApplicationContext(), "The description is the same than it  was before", Toast.LENGTH_LONG).show();
                else
                    updateNote(noteDescription, note);
                Toast.makeText(getApplicationContext(), "Completado", Toast.LENGTH_LONG).show();

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }


    /** CRUD actions **/

    private void createNewNote(String note){
        realm.beginTransaction();
        Note _note = new Note(note);
        realm.copyToRealm(_note);
        //aqui e entra la relacion.. y es donde se guarda todas las notas en el boards
        board.getNotes().add(_note);
        realm.commitTransaction();
    }


    private void updateNote(String noteDescription, Note note) {
        realm.beginTransaction();
        note.setDescription(noteDescription);
        realm.copyToRealmOrUpdate(note);
        realm.commitTransaction();
    }

    private void deleteNote(Note note) {

        realm.beginTransaction();
        note.deleteFromRealm();
        realm.commitTransaction();

    }

    private void deleteAllNotes() {
        realm.beginTransaction();
        board.getNotes().deleteAllFromRealm();
        realm.commitTransaction();
    }



    //** EVENTS **/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // delete all
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.delete_all_note:
                deleteAllNotes();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }




    // delete and update one a one

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        getMenuInflater().inflate(R.menu.context_menu_note_activity, menu);

        //super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()){
            case R.id.deleteNote:
                deleteNote(notes.get(info.position));
                return true;
            case R.id.editNote:
                showAlertForEditingNote("Edit board", "Change the name of the board", notes.get(info.position));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

// esto se crea al implementar el RealmChangeListener<Board> esto sirve para refrescar
    @Override
    public void onChange(Board element) {
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String m = String.valueOf(notes.get(position).getId());
        //Toast.makeText(getApplicationContext(), m ,Toast.LENGTH_SHORT).show();
        prueba("Elija una opcion", notes.get(position));

    }

}