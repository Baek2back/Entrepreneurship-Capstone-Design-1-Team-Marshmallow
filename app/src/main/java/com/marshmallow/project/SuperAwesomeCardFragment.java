/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.marshmallow.project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SuperAwesomeCardFragment extends Fragment {

	private static final String ARG_POSITION = "position";

	@BindView(R.id.textView)
    TextView textView;
	@BindView(R.id.imageView)
	ImageView imageView;

	private int position;

	public static SuperAwesomeCardFragment newInstance(int position) {
		SuperAwesomeCardFragment f = new SuperAwesomeCardFragment();
		Bundle b = new Bundle();
		b.putInt(ARG_POSITION, position);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		position = getArguments().getInt(ARG_POSITION);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card, container,false);
		ButterKnife.bind(this, rootView);
		ViewCompat.setElevation(rootView, 50);
		if(GlobalVariables.category == "history") {
			switch (position) {
				case 0:
					textView.setText(getString(R.string.seoae_ro));
					imageView.setImageResource(R.drawable.seoaero);
					Log.d("test", Integer.toString(position));
					break;
				case 1:
					textView.setText(getString(R.string.home_of_yooseongryong));
					imageView.setImageResource(R.drawable.yooseongryong);
					Log.d("test", Integer.toString(position));
					break;
				case 2:
					textView.setText(getString(R.string.korea_house));
					imageView.setImageResource(R.drawable.koreahouse);
					Log.d("test", Integer.toString(position));
					break;
				case 3:
					textView.setText(getString(R.string.namsangol_village));
					imageView.setImageResource(R.drawable.namsangol);
					Log.d("test", Integer.toString(position));
					break;
			}
		}
		else if(GlobalVariables.category == "movie")
		{
			switch (position) {
				case 0:
					textView.setText(getString(R.string.daehan_cinema));
					imageView.setImageResource(R.drawable.daehan);
					Log.d("test", Integer.toString(position));
					break;
				case 1:
					textView.setText(getString(R.string.ohzemi_film_studio));
					imageView.setImageResource(R.drawable.ohzemi);
					Log.d("test", Integer.toString(position));
					break;
				case 2:
					textView.setText(getString(R.string.white_pub));
					imageView.setImageResource(R.drawable.whitepub);
					Log.d("test", Integer.toString(position));
					break;
			}
		}
		return rootView;
	}
}