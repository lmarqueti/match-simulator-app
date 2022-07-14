package me.dio.simulator.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Random;

import me.dio.simulator.R;
import me.dio.simulator.data.MatchesApi;
import me.dio.simulator.databinding.ActivityMainBinding;
import me.dio.simulator.domain.Match;
import me.dio.simulator.ui.adapter.MatchesAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding binding;
    private MatchesApi matchesAPI;
    private MatchesAdapter matchesAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupHttpClient();
        setupMatchesList();
        setupMatchesRefresh();
        setupFloatingActionButton();

    }

    private void setupHttpClient() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://lmarqueti.github.io/match-simulator-api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        matchesAPI = retrofit.create(MatchesApi.class);
    }

    private void setupMatchesList() {
        binding.rvMatches.setHasFixedSize(true);
        binding.rvMatches.setLayoutManager(new LinearLayoutManager(this));
        findMatchesFromAPI();


    }

    private void setupMatchesRefresh() {
        binding.srlMatches.setOnRefreshListener(this::findMatchesFromAPI);
    }

    private void setupFloatingActionButton() {
        binding.fabSimulate.setOnClickListener(view ->{
            view.animate().rotationBy(360).setDuration(1000).setListener(new AnimatorListenerAdapter() {
               @Override
                public void onAnimationEnd(Animator animation) {
                    //super.onAnimationEnd(animation);
                   Random random = new Random();
                   for (int i = 0; i <matchesAdapter.getItemCount() ; i++) {
                       Match match = matchesAdapter.getMatches().get(i);
                       match.getHomeTeam().setScore(random.nextInt(match.getHomeTeam().getStars()+1));
                       match.getAwayTeam().setScore(random.nextInt(match.getAwayTeam().getStars()+1));
                       matchesAdapter.notifyItemChanged(i);
                   }
                }

            });
        });
    }

    private void findMatchesFromAPI() {
        binding.srlMatches.setRefreshing(true);
        matchesAPI.getMatches().enqueue(new Callback<List<Match>>() {
            @Override
            public void onResponse(Call<List<Match>> call, Response<List<Match>> response) {
                if (response.isSuccessful()) {
                    List<Match> matches = response.body();
                    matchesAdapter= new MatchesAdapter(matches);
                    binding.rvMatches.setAdapter(matchesAdapter);
                } else {
                    showErrorMassage();
                }
                binding.srlMatches.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Match>> call, Throwable t) {
                showErrorMassage();
                binding.srlMatches.setRefreshing(false);
            }
        });
    }

    private void showErrorMassage() {
        Snackbar.make(binding.fabSimulate,
                R.string.error_api,
                Snackbar.LENGTH_LONG).show();
    }

}
