package com.example.asadali.crazytipcalculator;

import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.util.Log;
import android.widget.Spinner;


public class CrazyTipCalc extends ActionBarActivity {

    //Save if app focus is gone!
    private static final String TOTAL_BILL = "TOTAL_BILL";
    private static final String CURRENT_TIP = "CURRENT_TIP";
    private static final String BILL_WITHOUT_TIP = "BILL_WITHOUT_TIP";

    private int[] checklistValues = new int[12];
    private double billBeforeTip;
    private double tipAmount;
    private double finalBill;

    EditText billBeforeTipET;
    EditText tipAmountET;
    EditText finalBillET;

    SeekBar tipSeekBar;

    CheckBox friendlyCheckBx;
    CheckBox specialCheckBx;
    CheckBox opinionCheckBx;

    RadioGroup availabilityRadioGrp;
    RadioButton badRadioBtn;
    RadioButton okRadioBtn;
    RadioButton goodRadioBtn;

    Spinner problemsSpinner;

    Chronometer timeWaitingChrono;

    Button startBtn;
    Button pauseBtn;
    Button resetBtn;

    long secondsYouWaited = 0;
    //changed
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crazy_tip_calc);

        if (savedInstanceState == null)//we are just starting the app
        {
            billBeforeTip = 0.0;
            tipAmount = 0.0;
            finalBill = 0.0;
        }else //coming from paused/restored state
        {
            billBeforeTip = savedInstanceState.getDouble(BILL_WITHOUT_TIP);
            tipAmount = savedInstanceState.getDouble(CURRENT_TIP);
            finalBill = savedInstanceState.getDouble(TOTAL_BILL);
        }

        billBeforeTipET = (EditText)findViewById(R.id.billEditText);
        tipAmountET = (EditText)findViewById(R.id.tipEditText);
        finalBillET = (EditText)findViewById(R.id.final_edit_text);

        tipSeekBar = (SeekBar)findViewById(R.id.tipSeekBar);

        friendlyCheckBx = (CheckBox)findViewById(R.id.friendlyCheckBox);
        specialCheckBx = (CheckBox)findViewById(R.id.specialsCheckBox);
        opinionCheckBx = (CheckBox)findViewById(R.id.opinionCheckBox);
        addCheckBoxChangeListeners();

        availabilityRadioGrp = (RadioGroup)findViewById(R.id.availabilityRadioGroup);
        badRadioBtn = (RadioButton)findViewById(R.id.availabilityBadRadioButton);
        okRadioBtn  = (RadioButton)findViewById(R.id.availabilityOkRadioButton);
        goodRadioBtn  = (RadioButton)findViewById(R.id.availabilityGoodRadioButton);
        addRadioChangeListeners();

        problemsSpinner = (Spinner)findViewById(R.id.problemsSpinner);
        addItemSelectedSpinnerListener();

        timeWaitingChrono = (Chronometer)findViewById(R.id.timeWaitingChronometer);
        startBtn = (Button)findViewById(R.id.startButton);
        pauseBtn = (Button)findViewById(R.id.pauseButton);
        resetBtn = (Button)findViewById(R.id.resetButton);
        addChronoBtnChangeListeners();

        //Add text changed listeners
        billBeforeTipET.addTextChangedListener(billBeforeTipListener);
        tipAmountET.addTextChangedListener(tipBeforeListener);
        tipSeekBar.setOnSeekBarChangeListener(tipSeekBarListener);

    }

    private void setTipFromWaitressChecklist()
    {
        int checklistTotal = 0;
        for(int item : checklistValues){
            checklistTotal += item;
        }
        tipAmountET.setText(String.format("%.02f", checklistTotal * 0.01));
    }

    private void addCheckBoxChangeListeners(){

        friendlyCheckBx.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checklistValues[0] = (friendlyCheckBx.isChecked()) ? 4: 0;
                setTipFromWaitressChecklist();
                updateTipAndFinalBill();
            }
        });

        specialCheckBx.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checklistValues[1] = (specialCheckBx.isChecked()) ? 1: 0;
                setTipFromWaitressChecklist();
                updateTipAndFinalBill();
            }
        });

        opinionCheckBx.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checklistValues[2] = (opinionCheckBx.isChecked()) ? 2: 0;
                setTipFromWaitressChecklist();
                updateTipAndFinalBill();
            }
        });
    }

    private void addRadioChangeListeners(){
        availabilityRadioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                checklistValues[3] = badRadioBtn.isChecked() ? -1:0;
                checklistValues[4] = okRadioBtn.isChecked() ? 1:0;
                checklistValues[5] = goodRadioBtn.isChecked()? 2: 0;
                setTipFromWaitressChecklist();
                updateTipAndFinalBill();
            }
        });
    }

    private void addItemSelectedSpinnerListener(){
        problemsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                checklistValues[6] = problemsSpinner.getSelectedItem().equals("Bad") ? -1: 0;
                checklistValues[7] = problemsSpinner.getSelectedItem().equals("Ok") ? 3 : 0;
                checklistValues[8] = problemsSpinner.getSelectedItem().equals("Good") ? 6 : 0;
                setTipFromWaitressChecklist();
                updateTipAndFinalBill();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void addChronoBtnChangeListeners(){
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int stoppedMilliseconds = 0;
                String chronoText = timeWaitingChrono.getText().toString();
                String array[] = chronoText.split(":");
                if (array.length == 2)
                {
                    stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 1000 +
                            Integer.parseInt(array[1]) * 1000;
                }
                else if (array.length == 3){
                    stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 60 * 1000 +
                            Integer.parseInt(array[0]) * 60 * 1000 +
                            Integer.parseInt(array[1]) * 1000;

                }
                timeWaitingChrono.setBase(SystemClock.elapsedRealtime() - stoppedMilliseconds);
                secondsYouWaited = Long.parseLong(array[1]);
                updateTipBasedOnTimeWaited(secondsYouWaited);
                timeWaitingChrono.start();
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeWaitingChrono.stop();
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeWaitingChrono.setBase(SystemClock.elapsedRealtime());
                secondsYouWaited = 0;
            }
        });
    }

    private void updateTipBasedOnTimeWaited(long secondsWaited) {
        checklistValues[9] = (secondsWaited > 10)? -2: 2;
        setTipFromWaitressChecklist();
        updateTipAndFinalBill();
    }
    //added
    private TextWatcher billBeforeTipListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            try{
                billBeforeTip = Double.parseDouble(charSequence.toString());
            }catch (NumberFormatException n){
                billBeforeTip = 0.0;
            }
            updateTipAndFinalBill();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private TextWatcher tipBeforeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            try
            {
               tipAmount = Double.parseDouble(charSequence.toString());
            }
            catch (NumberFormatException n)
            {
                tipAmount = 0.15;
            }
            updateTipAndFinalBill();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private SeekBar.OnSeekBarChangeListener tipSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            tipAmount = (tipSeekBar.getProgress()) * .01;
Log.w("CrazyTipCalc---->", String.valueOf(tipAmount));
            tipAmountET.setText(String.valueOf(tipAmount));
            updateTipAndFinalBill();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void updateTipAndFinalBill()
    {
        double tipAmount = Double.parseDouble(tipAmountET.getText().toString());
        double finalBill = tipAmount * billBeforeTip + billBeforeTip;
        finalBillET.setText(String.format("%.02f", finalBill));
        //tipSeekBar.setProgress((int)tipAmount * 100);
    }

    //called when device is rotated, switch to another app etc... ie when this
    //app looses focus
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putDouble(TOTAL_BILL, finalBill);
        outState.putDouble(CURRENT_TIP, tipAmount);
        outState.putDouble(BILL_WITHOUT_TIP, billBeforeTip);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.crazy_tip_calc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
