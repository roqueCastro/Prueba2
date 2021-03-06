package com.example.asus.prueba2.activities;

import android.content.DialogInterface;
import android.content.Intent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.asus.prueba2.R;
import com.example.asus.prueba2.adapters.BoardAdapter;
import com.example.asus.prueba2.models.Board;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class BoardActivity extends AppCompatActivity implements RealmChangeListener<RealmResults<Board>>, AdapterView.OnItemClickListener {
    // RealmChangeListener<RealmResults<Board>> se implementa REFRESH_ADAP_p2

    // , AdapterView.OnItemClickListener se implementa para pasar a la otra activity START ONCLIK_ADAP_VIEW_p1

    private Realm realm;

    private FloatingActionButton fab;
    private ListView listView;
    private BoardAdapter adapter;

    private RealmResults<Board> boards;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        //Db Realm
        realm = Realm.getDefaultInstance();
        boards = realm.where(Board.class).findAll();              // Recoger los resultados con la consulta
        boards.addChangeListener(this);// refreshd del adapter START REFRESH_ADAP_p1

        adapter = new BoardAdapter(this, boards, R.layout.list_view_board_item);
        listView =  (ListView) findViewById(R.id.listViewBoard);         //* CAPTURA el lisView *//
        listView.setAdapter(adapter);      // SE LE AJUNTA EL ADAPTADOR A LA LISTA
        listView.setOnItemClickListener(this); // ajuntamos para enviar el id a la otra actividad

        fab = (FloatingActionButton) findViewById(R.id.fabAddBoard);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertForCreatingBoard("Add new board", "Type a  name for your new board");
            }
        });




        registerForContextMenu(listView);
    }

    /** CRUD actions **/
    private void createNewBoard(String boardName) {
        realm.beginTransaction();
        Board board = new Board(boardName);
        realm.copyToRealm(board);
        realm.commitTransaction();
    }

    private void updateBoard(String newName, Board board) {
        realm.beginTransaction();
        board.setTitle(newName);
        realm.copyToRealmOrUpdate(board);
        realm.commitTransaction();
    }

    private void deleteBoard(Board board) {

        realm.beginTransaction();
        board.deleteFromRealm();
        realm.commitTransaction();

    }

    private void deleteAll() {
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }


    /**    Dialogs ALERT INSERT **/
    private void showAlertForCreatingBoard(String title, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_board, null);
        builder.setView(viewInflated);

        final EditText input = (EditText) viewInflated.findViewById(R.id.editTextNewBoard);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                  String boardName = input.getText().toString().trim();
                if(boardName.length() > 0)
                    createNewBoard(boardName);
                else
                    Toast.makeText(getApplicationContext(), "The name is required to create new board", Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }



    private void showAlertPrincipal(String title, final Board board) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (title != null) builder.setTitle(title);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_board_principal, null);
        builder.setView(viewInflated);

        final AlertDialog dialog = builder.create();


        final Button btnAgregarNota = (Button) viewInflated.findViewById(R.id.btn_board_add_note);
        final Button btnDeleteBoard = (Button) viewInflated.findViewById(R.id.btn_board_delete);
        final Button btnUpdateBoard = (Button) viewInflated.findViewById(R.id.btn_board_update);

        btnAgregarNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Agregando Notas",Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(BoardActivity.this, NoteActivity.class);  // para dirigirnos de la clase donde stamos a la otra
                intent.putExtra("id", board.getId());
                startActivity(intent);

                dialog.cancel();
            }
        });

        btnDeleteBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Eliminando Board",Toast.LENGTH_SHORT).show();

                deleteBoard(board);

                dialog.cancel();
            }
        });

        btnUpdateBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Actualizando Board",Toast.LENGTH_SHORT).show();

                showAlertForEditingBoard("Editar board", "Cambiar el nombre del board", board);

                dialog.cancel();
            }
        });


        dialog.show();

    }


        private void showAlertForEditingBoard(String title, String message, final Board board) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_board, null);
        builder.setView(viewInflated);

        final EditText input = (EditText) viewInflated.findViewById(R.id.editTextNewBoard);
        input.setText(board.getTitle());

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String boardName = input.getText().toString().trim();

                if (boardName.length() == 0)
                    Toast.makeText(getApplicationContext(), "The name is required to edit the current board", Toast.LENGTH_LONG).show();
                else if (boardName.equals(board.getTitle()))
                Toast.makeText(getApplicationContext(), "The name is the same than it  was before", Toast.LENGTH_LONG).show();
                else
                    updateBoard(boardName, board);

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    //** EVENTS **/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_board_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // delete all
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.delete_all:
                deleteAll();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    // delete and update one a one

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(boards.get(info.position).getTitle());
        getMenuInflater().inflate(R.menu.context_menu_board_activity, menu);

        //super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()){
            case R.id.delete_board:
                deleteBoard(boards.get(info.position));
                return true;
            case R.id.edit_board:
                showAlertForEditingBoard("Edit board", "Change the name of the board", boards.get(info.position));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    //* REFRESH_ADAP_p3  */
    @Override
    public void onChange(RealmResults<Board> element) {
        adapter.notifyDataSetChanged();
    }


    //**  ONCLIK_ADAPTER_VIEW_p2  *//
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        showAlertPrincipal("Elija solo una opción", boards.get(position));

        /*Intent intent = new Intent(BoardActivity.this, NoteActivity.class);  // para dirigirnos de la clase donde stamos a la otra
        intent.putExtra("id", boards.get(position).getId());
        startActivity(intent);*/


    }
}
