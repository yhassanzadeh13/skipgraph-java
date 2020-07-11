package underlay.packets;

import skipnode.SkipNodeIdentity;

public class UpdateRequest implements RequestParameters {
    private int level;
    private SkipNodeIdentity snId;

    public UpdateRequest(int level, SkipNodeIdentity snId){
        this.level=level;
        this.snId=snId;
    }

    @Override
    public Object getRequestValue(String parameterName) {
        if(parameterName.equalsIgnoreCase("newValue")){
            return snId;
        }else if(parameterName.equalsIgnoreCase("level")){
            return level;
        }
        return null;
    }
}
