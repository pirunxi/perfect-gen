package gencfg.data;

public interface Visitor {

	void accept(FBool fBool);

	void accept(FFloat fFloat);

	void accept(FDouble d);

	void accept(FInt fInt);

	void accept(FList fList);

	void accept(FLong fLong);

	void accept(FMap fMap);

	void accept(FSet fSet);

	void accept(FString fString);

	void accept(FBean fStruct);

	void accept(FEnum fEnum);

}
