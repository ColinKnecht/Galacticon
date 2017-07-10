/*
 * Copyright (c) 2016 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.galacticon;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ImageRequester.ImageRequesterResponse {

  private ArrayList<Photo> mPhotosList;
  private ImageRequester mImageRequester;
  private RecyclerView mRecyclerView;
  private LinearLayoutManager mLinearLayoutManager;
  private RecyclerAdapter mAdapter;
  private GridLayoutManager mGridLayoutManager;//extra

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);//connects to xml activity_main
    mLinearLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLinearLayoutManager);//connects recycler view to mLinearLayoutManager
    mGridLayoutManager = new GridLayoutManager(this, 2);//extra

    mPhotosList = new ArrayList<>();
    mImageRequester = new ImageRequester(this);

    //BELOW:Here you’re creating the adapter, passing in the constructors it needs and setting it as the adapter for your RecyclerView.
    mAdapter = new RecyclerAdapter(mPhotosList);
    mRecyclerView.setAdapter(mAdapter);

    setRecyclerViewScrollListener();
    setRecyclerViewItemTouchListener();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  protected void onStart() {
    super.onStart();

    if (mPhotosList.size() == 0) {//This adds a check to see if your list is empty, and if yes, it requests a photo.
      requestPhoto();
    }

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    //This checks the ID of the item tapped in the menu, then works out what to do about it. In this case, there should
    // only be one ID that will match up, effectively telling the app to go away and rearrange the RecyclerView’s LayoutManager.
    if (item.getItemId() == R.id.action_change_recycler_manager) {
      changeLayoutManager();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
  private int getLastVisibleItemPosition() { //This uses your RecyclerView’s LinearLayoutManager to get the index of the last visible item on the screen.
    int itemCount;
    //Here you ask the RecyclerView to tell you what its LayoutManager is, then you ask that LayoutManager to tell you the position of the last visible item.
    if (mRecyclerView.getLayoutManager().equals(mLinearLayoutManager)) {
      itemCount = mLinearLayoutManager.findLastVisibleItemPosition();
    } else {
      itemCount = mGridLayoutManager.findLastVisibleItemPosition();
    }

    return itemCount;
  }
  private void setRecyclerViewScrollListener() {
    //Now the RecyclerView has a scroll listener attached to it that is triggered by scrolling. During scrolling, the listener retrieves the count of the items
    // in its LayoutManager and calculates the last visible photo index. Once done, it compares these numbers (incrementing the index by 1 because the index begins at
    // 0 while the count begins at 1). If they match and there are no photos already on request, then you request a new photo.
    mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        int totalItemCount = mRecyclerView.getLayoutManager().getItemCount();
        if (!mImageRequester.isLoadingData() && totalItemCount == getLastVisibleItemPosition() + 1) {
          requestPhoto();
        }
      }
    });
  }

  private void setRecyclerViewItemTouchListener() {

    //1 You create the callback and tell it what events to listen for. It takes two parameters, one for drag directions and one for swipe directions,
    // but you’re only interested in swipe, so you pass 0 to inform the callback not to respond to drag events.
    ItemTouchHelper.SimpleCallback itemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
      @Override
      public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder1) {
        //2 You return false in onMove because you don’t want to perform any special behavior here.
        return false;
      }

      @Override
      public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
        //3 onSwiped is called when you swipe an item in the direction specified in the ItemTouchHelper. Here, you request the viewHolder parameter
        // passed for the position of the item view, then you remove that item from your list of photos. Finally, you inform the RecyclerView adapter
        // that an item has been removed at a specific position.
        int position = viewHolder.getAdapterPosition();
        mPhotosList.remove(position);
        mRecyclerView.getAdapter().notifyItemRemoved(position);
      }
    };

    //4 You initialize the ItemTouchHelper with the callback behavior you defined, and then attach it to the RecyclerView.
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
    itemTouchHelper.attachToRecyclerView(mRecyclerView);
  }
  private void changeLayoutManager() { //extra
    //This code checks to see what LayoutManager your RecyclerView is using, and then:
    //1.If it’s using the LinearLayoutManager, it swaps in the GridLayoutManager
    //2.It requests a new photo if your grid layout only has one photo to show
    //3. If it’s using the GridLayoutManager, it swaps in the LinearLayoutManager
    if (mRecyclerView.getLayoutManager().equals(mLinearLayoutManager)) {
      //1
      mRecyclerView.setLayoutManager(mGridLayoutManager);
      //2
      if (mPhotosList.size() == 1) {
        requestPhoto();
      }
    } else {
      //3
      mRecyclerView.setLayoutManager(mLinearLayoutManager);
    }
  }
  private void requestPhoto() {

    try {
      mImageRequester.getPhoto();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void receivedNewPhoto(final Photo newPhoto) {

    //Here you are informing the recycler adapter that an item was added after the list of photos was updated.
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mPhotosList.add(newPhoto);
        mAdapter.notifyItemInserted(mPhotosList.size());
      }
    });
  }
}
