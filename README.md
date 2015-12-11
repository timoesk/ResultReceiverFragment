# ResultReceiverFragment
Android fragment for receiving results from services. Instance state is retained.

## Usage

### Extend ResultReceiverFragment
```java
    public static class DataFragment extends ResultReceiverFragment {

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
          // Handle result
        }

        @Override
        protected void onUpdate(int updateCode) {
          // Handle local update broadcast
        }
    }
```

### Attach extended fragment to activity
```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        dataFragment = (DataFragment) fm.findFragmentByTag(DataFragment.TAG);
        if (dataFragment == null) {
            dataFragment = new DataFragment();
            fm.beginTransaction().add(dataFragment, DataFragment.TAG).commit();
            // Load data by starting i.e. intent service
            Intent intent = new Intent(this, DataService.class);
            intent.putExtra(EXTRA_RESULT_RECEIVER, dataFragment.getResultReceiver());
            startService(intent);
        }
    }
```

### Deliver results and send local update broadcasts from service
```java
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            // Get result receiver
            final ResultReceiver resultReceiver = intent.getParcelableExtra(EXTRA_RESULT_RECEIVER);
            // Put data to bundle
            Bundle data = new Bundle();
            data.putInt(RESULT_DATA_INT, 1);
            // Send result
            resultReceiver.send(RESULT_CODE_INT, data);
            // Send update broadcast
            ResultReceiverFragment.sendUpdate(this, UPDATE_CODE_INT);
        }
    }
```

### Dependencies
```
    compile 'net.tolleri.android:resultreceiverfragment:1.0.0'
```

## License
```
Copyright 2015 Timo Eskola

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
