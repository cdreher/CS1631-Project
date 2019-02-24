import java.util.*;


private int key;    //ID number / VoterPhoneNumber
private int value;  //Candidate ID
private int count;  //vote counts


public class ValuePair {


    //constructor

    //For TallyTable
    public ValuePair(int key, int value){

        this.key = key;
        this.value = value;

    }

    //For VoterTable
    public ValuePair(int key, int value, int count){

        this.key = key;
        this.value = value;
        this.count = count;

    }


    //return the ID number/VoterPhone number
    public int getKey(){
        return this.key;
    }

    //return the Candidate ID
    public int getValue(){
        return this.value;
    }

    //return the vote count
    public int getCount{
        return this.count;
    }

    //set new ID number/VoterPhone number
    public void setKey(int key){
        this.key = key;
    }

    //set new Candidate ID
    public void setValue(int value){
        this.value = value;
    }

    public void setCount(int count){
        this.count = count;
    }

    //increment the Candidate ID
    public int addCount(){
        this.count ++;
        return this.count;
    }
}