package sp.phone.adapter;

import gov.anzong.androidnga.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sp.phone.bean.ThreadPageInfo;
import sp.phone.bean.TopicListInfo;
import sp.phone.interfaces.NextJsonTopicListLoader;
import sp.phone.task.JsonTopicListLoadTask;
import sp.phone.utils.ActivityUtil;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshAttacher;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class AppendableTopicAdapter extends TopicListAdapter {
	final private List<TopicListInfo> infoList;
    final private PullToRefreshAttacher attacher;
    private final NextJsonTopicListLoader loader;
    private boolean isEndOfList = false;
    private boolean isPrompted = false;
	Set<Integer> tidSet;
	private int table;
	public AppendableTopicAdapter(Context context,PullToRefreshAttacher attacher,NextJsonTopicListLoader loader ) {
		super(context);
		infoList = new ArrayList<TopicListInfo>();
		tidSet = new HashSet<Integer>();
        this.attacher = attacher;
        this.loader = loader;
	}

	@Override
	protected ThreadPageInfo getEntry(int position) {
		for(int i=0; i< infoList.size(); i++){
			if(position < infoList.get(i).get__T__ROWS()){
				return infoList.get(i).getArticleEntryList().get(position);
			}
			position -= infoList.get(i).get__T__ROWS();
		}
		return null;
	}

	@Override
	public void jsonfinishLoad(TopicListInfo result) {
        isLoading = false;
        table=result.get__TABLE();
        if(attacher !=null)
            attacher.setRefreshComplete();
        if(result.get__SEARCHNORESULT()){
        	if(table>0){
    			isEndOfList = true;
    			isLoadingTable=true;
    			TableListPage=infoList.size();
    			TableList__ROWS=count;
        		Toast.makeText(this.context,
					"库"+String.valueOf(table)+"中的结果已搜索完毕,正在搜索库"+String.valueOf(table-1)+"中的结果",
					Toast.LENGTH_SHORT).show();
        		isLoading = true;
        		loader.loadNextPage(this);
            }else{
                	Toast.makeText(this.context,
        					"所有数据库结果已搜索完毕",
        					Toast.LENGTH_SHORT).show();
                isLoading = false;
            }
            return;
        }
        if(result == null){
            return;
        }
        ActivityUtil.getInstance().dismiss();

		if (count != 0) {
			List<ThreadPageInfo> threadList = new ArrayList<ThreadPageInfo>();
			for (int i = 0; i < result.getArticleEntryList().size(); i++) {
				ThreadPageInfo info = result.getArticleEntryList().get(i);
				if(info == null){
					continue;
				}
				int tid = info.getTid();
				if (!tidSet.contains(tid)) {
					threadList.add(info);
					tidSet.add(tid);
				}
			}
			result.set__T__ROWS(threadList.size());
			result.setArticleEntryList(threadList);
		}else{
			for (int i = 0; i < result.getArticleEntryList().size(); i++) {
				ThreadPageInfo info = result.getArticleEntryList().get(i);
				if(info == null){
					continue;
				}
				int tid = info.getTid();
				tidSet.add(tid);
			}
			
		}

		infoList.add(result);
		count += result.get__T__ROWS();
		if (count >= (result.get__ROWS()+TableList__ROWS))
		{
			isEndOfList = true;
		}else{
			isEndOfList = false;
		}
		
		if(count != (result.get__T__ROWS()+TableList__ROWS)||isLoadingTable)
		{

			this.notifyDataSetChanged();
			
			//Toast.makeText(context, "finish load page:" + infoList.size(), Toast.LENGTH_SHORT).show();
		}
		
	}
	
	public void clear(){
		count = 0;
		infoList.clear();
		tidSet.clear();
		setSelected(-1);
	}
	
	public int getNextPage(){
		if(!isLoadingTable){
			return infoList.size() + 1;
		}else{
			return infoList.size()-TableListPage+1;
		}
	}
	
	public boolean getIsEnd(){
		return isEndOfList;
	}

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View ret = super.getView(position, view, parent);
        if( position +1 == this.getCount() && !isLoading){
        	if (isEndOfList == true)
        	{
        		if(table<2||table>7){//数据库没有或者加载完了
        		if (isPrompted == false) {
					Toast.makeText(this.context,
							context.getString(R.string.last_page_prompt),
							Toast.LENGTH_SHORT).show();
					isPrompted = true;
				}
        		}else{
        			TableListPage=infoList.size();
        			isLoadingTable=true;
        			TableList__ROWS=count;
        			Toast.makeText(this.context,
							"库"+String.valueOf(table-1)+"中的结果已搜索完毕,正在搜索库"+String.valueOf(table-2)+"中的结果",
							Toast.LENGTH_SHORT).show();
                    isLoading = true;
                    loader.loadNextPage(this);
        		}
        	}
        	else {
            isLoading = true;
            loader.loadNextPage(this);
        	}
        }
        return  ret;
    }
    private boolean isLoading = false;
    private int TableList__ROWS = 0;
    private boolean isLoadingTable = false;
    private int TableListPage=0;
}
