package fr.eilco.booksprojects.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import fr.eilco.booksprojects.R;
import fr.eilco.booksprojects.adapters.BookListAdapter;
import fr.eilco.booksprojects.data.AuthorData;
import fr.eilco.booksprojects.data.BookData;
import fr.eilco.booksprojects.database.AppDatabase;
import fr.eilco.booksprojects.database.BookDao;
import fr.eilco.booksprojects.model.Author;
import fr.eilco.booksprojects.model.Book;
import fr.eilco.booksprojects.serverOperations.GetRequest;
import fr.eilco.booksprojects.serverOperations.callback.AuthorGetRequestCallback;
import fr.eilco.booksprojects.serverOperations.callback.BookGetRequestCallback;
import fr.eilco.booksprojects.serverOperations.callback.ImageBookGetRequestCallback;


public class BookListActivity extends AppCompatActivity {

    private RecyclerView rvRandomList;
    private RecyclerView rvNewBooks;
    private RecyclerView rvFavorite;
    private int cardViewId = R.layout.view_book_card;
    private BookListAdapter adapterRandomList;
    private BookListAdapter adapterNewList;
    private BookListAdapter adapterFavorite;
    public static BookData randomList;
    public static BookData newList;

    public static BookData favoriteList;


    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        ImageView ivSearchImageView = findViewById(R.id.ivSearch);
        ivSearchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookListActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        GetRequest getRequest = new GetRequest();
        GetRequest getRequest2 = new GetRequest();

        rvRandomList = findViewById(R.id.rvRandomList);
        rvNewBooks = findViewById(R.id.rvNewBooks);
        rvFavorite = findViewById(R.id.rvFavorites);

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        // Obtenez les DAO KeyValueDao et BookDao
        BookDao bookDao = db.bookDao();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                List<Book> favoriteBooks= bookDao.getAllBooks();
                favoriteList = new BookData(favoriteBooks);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                adapterFavorite = new BookListAdapter(cardViewId, favoriteList.getBooks(),"FavoriteList");
                rvFavorite.setLayoutManager(new LinearLayoutManager(BookListActivity.this, LinearLayoutManager.HORIZONTAL, false));
                rvFavorite.setAdapter(adapterFavorite);
                super.onPostExecute(aVoid);
            }
        }.execute();


        String randomWord = UtilsWord.getRandomWord(this);
        TextView tvRandomWord = findViewById(R.id.tvRandomWord);
        if(!randomWord.isEmpty()){
            tvRandomWord.setText(randomWord);
        }
        Log.i("RANDOM",randomWord);
        getRequest.retrBooks(randomWord, 15, new BookGetRequestCallback() {
            @Override
            public void onSuccess(List<Book> books) {

                randomList = new BookData(books);
                adapterRandomList = new BookListAdapter(cardViewId, new ArrayList<>(books),"RandomList");

                rvRandomList.setLayoutManager(new LinearLayoutManager(BookListActivity.this, LinearLayoutManager.HORIZONTAL, false));
                rvRandomList.setAdapter(adapterRandomList);

                for (Book bookItem:randomList.getBooks()) {
                    GetRequest getRequestImage = new GetRequest();
                    getRequestImage.retrBookImage(bookItem, new ImageBookGetRequestCallback() {
                        @Override
                        public void onSuccess(Bitmap image) {
                            rvRandomList.setAdapter(adapterRandomList);
                        }

                        @Override
                        public void onFailure() {
                        }
                    });

                    String authorKey = bookItem.getAuthorKey();
                    if(!AuthorData.getInstance().haveAuthor(authorKey)){
                        GetRequest getRequestAuthor = new GetRequest();

                        getRequestAuthor.retrAuthor(authorKey, new AuthorGetRequestCallback() {
                            @Override
                            public void onSuccess(Author author) {
                                author.setKey(authorKey);
                                AuthorData.getInstance().addAuthor(author);
                            }

                            @Override
                            public void onFailure(Throwable t) {
                            }
                        });
                    }

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("Request DEBUG", t.getMessage());
            }
        });


        getRequest2.retrBooks("The Lord of the Rings", 15,  new BookGetRequestCallback() {
            @Override
            public void onSuccess(List<Book> books) {

                newList = new BookData(books);

                adapterNewList = new BookListAdapter(cardViewId, new ArrayList<>(books),"NewBooks");
                rvNewBooks.setLayoutManager(new LinearLayoutManager(BookListActivity.this, LinearLayoutManager.HORIZONTAL, false));
                rvNewBooks.setAdapter(adapterNewList);

                for (Book bookItem:newList.getBooks()) {
                    GetRequest getRequestImage = new GetRequest();
                    getRequestImage.retrBookImage(bookItem, new ImageBookGetRequestCallback() {
                        @Override
                        public void onSuccess(Bitmap image) {
                            rvNewBooks.setAdapter(adapterNewList);
                        }

                        @Override
                        public void onFailure() {

                        }
                    });

                    String authorKey = bookItem.getAuthorKey();
                    if(!AuthorData.getInstance().haveAuthor(authorKey)){
                        GetRequest getRequestAuthor = new GetRequest();

                        getRequestAuthor.retrAuthor(authorKey, new AuthorGetRequestCallback() {
                            @Override
                            public void onSuccess(Author author) {
                                author.setKey(authorKey);
                                AuthorData.getInstance().addAuthor(author);
                            }

                            @Override
                            public void onFailure(Throwable t) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                ;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        adapterFavorite = new BookListAdapter(cardViewId, favoriteList.getBooks(), "FavoriteList");
        rvFavorite.setLayoutManager(new LinearLayoutManager(BookListActivity.this, LinearLayoutManager.HORIZONTAL, false));
        rvFavorite.setAdapter(adapterFavorite);
    }

}
