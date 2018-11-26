/**
 * 
 */
package com.game.chess.spar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.game.chess.R;
import com.game.chess.adapter.CommonAdapter;
import com.game.chess.model.AIPosition;
import com.game.chess.model.Chess;
import com.game.chess.model.ViewHolder;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

/**
 * @author LJY
 * 
 * @create_time 2017-12-1
 * 
 */
public class MainActivity extends Activity implements OnItemClickListener,
		CompoundButton.OnCheckedChangeListener,OnClickListener{
	/** 上下文对象 */
	private Context mContext = MainActivity.this;
	/** adapter */
	private CommonAdapter<Chess> adapter;
	/** 数据源 */
	private ArrayList<Chess> mData = new ArrayList<Chess>();
	private ArrayList<Integer> dataRepeat = new ArrayList<Integer>();
	//AI开启后记录每次最佳落子集合，目的在于点击悔棋操作优先级能正常体现
	private ArrayList<TreeSet<AIPosition>> sets = new ArrayList<TreeSet<AIPosition>>();
	private ArrayList<TreeSet<AIPosition>> attackSets = new ArrayList<TreeSet<AIPosition>>();
	private boolean isRepeat = false;
	private boolean isRepeatEnd = true;
	private boolean isEnd = false;
	private int[][] chessBoard = null;
	private GridView chessGrid = null;
	TextView textview_notify;
	CheckBox checkboxAI;
	// 是否开启人工智能 true为开启
	private boolean isAIOpen = true;
	/** 默认黑棋先手 */
	private boolean whoMove = true;
	
	TreeSet<AIPosition> attakSet = new TreeSet<AIPosition>();//进攻一阵
	TreeSet<AIPosition> set = new TreeSet<AIPosition>();//一阵
	private final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gomoku);
		initView();
		initData();
		initEvent();
	}

	private void initView() {
		chessGrid = (GridView) findViewById(R.id.gridview);
		checkboxAI = (CheckBox) findViewById(R.id.checkboxAI);
		textview_notify = (TextView) findViewById(R.id.textview_notify);
	}

	private void initData() {
		// 初始化复盘集合
		readData();
		initAdapter();
		chessGrid.setAdapter(adapter);
	}

	private void readData() {
		attakSet.clear();
		mData = new ArrayList<Chess>();
		chessBoard = new int[16][14];
		for (int i = 0; i < 224; i++) {
			Chess chess = new Chess();
			chess.who = 0;
			mData.add(chess);
		}
	}

	private void initAdapter() {
		adapter = new CommonAdapter<Chess>(mContext, mData, R.layout.item) {

			@Override
			public void convert(ViewHolder helper, int pos) {

				final ImageView chessImageView = helper
						.getView(R.id.chessImageView);

				Chess chess = mData.get(pos);
				switch (chess.who) {
				case 0:
					chessImageView.setVisibility(View.INVISIBLE);
					break;
				case 1:
					chessImageView.setVisibility(View.VISIBLE);
					chessImageView.setImageResource(mData.get(pos).CHESS_BLACK);
					break;
				case 2:
					chessImageView.setVisibility(View.VISIBLE);
					chessImageView.setImageResource(mData.get(pos).CHESS_WHITE);
					break;
				}
			}
		};
	}

	public void restart(View view) {
		readData();
		adapter.notifyDataSetChanged();
		isRepeat = false;
		isRepeatEnd = true;
		isEnd = false;
		whoMove = true;
		attakSet.clear();
		set.clear();
		dataRepeat.clear();
		sets.clear();
		attackSets.clear();
	}

	/**
	 * 悔棋操作
	 * */
	public void regret(View view) {
		if (isEnd) {
			Toast toast = Toast.makeText(mContext, "游戏已经结束，重新开一局吧~", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}else if ((isAIOpen && dataRepeat.size() < 2)//人机
				|| (!isAIOpen && whoMove && dataRepeat.size() == 0)//黑子
				|| (!isAIOpen && !whoMove && dataRepeat.size() < 2)//白子
				) {
			Toast toast = Toast.makeText(mContext, "您还没下棋呐~", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}else {
			Builder builder = new Builder(this, AlertDialog.THEME_HOLO_LIGHT);
			builder.setTitle("提示");
			builder.setMessage("确认悔棋？");
			builder.setPositiveButton("确认",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						int pos = dataRepeat.get(dataRepeat.size()-1);
						int xy[]= postion2XY(pos);
						if (isAIOpen && whoMove) {//机器人对战模式悔棋移除两颗棋子
							//移除机器人
							chessBoard[xy[1]][xy[0]] = 0;
							dataRepeat.remove(dataRepeat.size()-1);
							mData.get(pos).who = 0;
							//移除用户
							pos = dataRepeat.get(dataRepeat.size()-1);
							xy = postion2XY(pos);
							chessBoard[xy[1]][xy[0]] = 0;
							dataRepeat.remove(dataRepeat.size()-1);
							mData.get(pos).who = 0;
							if (sets.size() >= 1) {
								sets.remove(sets.size()-1);
								if (sets.size() >= 1) {
									set = sets.get(sets.size()-1);
								}else {
									set.clear();
								}

							}
							if (attackSets.size() >= 1) {
								attackSets.remove(attackSets.size()-1);
								if (attackSets.size() >= 1) {
									attakSet = attackSets.get(attackSets.size()-1);
								}else {
									attakSet.clear();
								}

							}

						}else {
							chessBoard[xy[1]][xy[0]] = 0;
							dataRepeat.remove(dataRepeat.size()-1);
							mData.get(pos).who = 0;
						}
						
						if (whoMove || isAIOpen) {
							textview_notify.setText("请黑方落子");
							whoMove = true;
						} else {
							textview_notify.setText("请白方落子");
						}
						adapter.notifyDataSetChanged();
					}
				});
			builder.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});
			builder.create();
			builder.show();
		}
	}

	/**
	 * 复盘
	 */
	public void repeat(View view) {
		readData();
		isEnd = true;
		checkboxAI.setEnabled(false);
		findViewById(R.id.id_regret).setEnabled(false);
		findViewById(R.id.id_restart).setEnabled(false);
		findViewById(R.id.id_repeat).setEnabled(false);
		adapter.notifyDataSetChanged();
		isRepeat = true;
		isRepeatEnd = false; 
		whoMove = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				repeatFor: for (int position : dataRepeat) {
					Message msg = handler.obtainMessage();
					msg.arg1 = position;
					handler.sendMessage(msg);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (isRepeatEnd) {
						break repeatFor;
					}
				}
				isRepeatEnd = true;
				Message msg = handler.obtainMessage();
				msg.what = 1171;
				handler.sendMessage(msg);
			}
		}).start();
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			if (msg.what == 1171) {
				checkboxAI.setEnabled(true);
				findViewById(R.id.id_regret).setEnabled(true);
				findViewById(R.id.id_restart).setEnabled(true);
				findViewById(R.id.id_repeat).setEnabled(true);
			}else {
				onItemClick(chessGrid, null, msg.arg1, msg.arg1);
			}
		}
	};

	private void initEvent() {
		chessGrid.setOnItemClickListener(this);
		checkboxAI.setOnCheckedChangeListener(this);
		findViewById(R.id.id_regret).setOnClickListener(this);
		findViewById(R.id.id_restart).setOnClickListener(this);
		findViewById(R.id.id_repeat).setOnClickListener(this);
	}

	/**
	 * @chess 落点位置是否合法
	 * */
	private boolean isValid(Chess chess) {
		return chess.who == 0;
	}
	
	/**
	 * 
	 * @param xy
	 * @return
	 */
	//st isContinue
	public boolean isContinue(int[] xy) {
		int continuousCount = 1;
		// 上
		int x = xy[1];
		int y = xy[0] - 1;

		while (y >= 0 && chessBoard[x][y] == chessBoard[xy[1]][xy[0]]) {
			continuousCount++;
			y--;
		}
		// 下
		x = xy[1];
		y = xy[0] + 1;
		while (y < chessBoard[x].length
				&& chessBoard[x][y] == chessBoard[xy[1]][xy[0]]) {
			continuousCount++;
			y++;
		}

		if (continuousCount >= 5) {
			return false;
		}

		// 左
		x = xy[1] - 1;
		y = xy[0];
		continuousCount = 1;
		while (x >= 0 && chessBoard[x][y] == chessBoard[xy[1]][xy[0]]) {
			continuousCount++;
			x--;
		}
		// 右
		x = xy[1] + 1;
		y = xy[0];
		while (x < chessBoard.length
				&& chessBoard[x][y] == chessBoard[xy[1]][xy[0]]) {
			continuousCount++;
			x++;
		}

		if (continuousCount >= 5) {
			return false;
		}

		// 左上
		x = xy[1] - 1;
		y = xy[0] - 1;
		continuousCount = 1;
		while (x >= 0 && y >= 0 && chessBoard[x][y] == chessBoard[xy[1]][xy[0]]) {
			continuousCount++;
			x--;
			y--;
		}
		// 右下
		x = xy[1] + 1;
		y = xy[0] + 1;
		while (x < chessBoard.length && y < chessBoard[x].length
				&& chessBoard[x][y] == chessBoard[xy[1]][xy[0]]) {
			continuousCount++;
			x++;
			y++;
		}

		if (continuousCount >= 5) {
			return false;
		}

		// 左下
		x = xy[1] - 1;
		y = xy[0] + 1;
		continuousCount = 1;
		while (x >= 0 && y < chessBoard[x].length
				&& chessBoard[x][y] == chessBoard[xy[1]][xy[0]]) {
			continuousCount++;
			x--;
			y++;
		}
		// 右上
		x = xy[1] + 1;
		y = xy[0] - 1;
		while (x < chessBoard.length && y >= 0
				&& chessBoard[x][y] == chessBoard[xy[1]][xy[0]]) {
			continuousCount++;
			x++;
			y--;
		}

		if (continuousCount >= 5) {
			return false;
		}

		return true;
	}
	//ed isContinue

	/**
	 * 将position转化为二维坐标
	 * 
	 * @param postion
	 * @return
	 */
	public int[] postion2XY(int postion) {
		int[] xy = new int[2];
		xy[0] = postion / 14;
		xy[1] = postion % 14;
		return xy;
	}

	/**
	 * 将XY坐标转化为position
	 */
	public int xy2Position(int[] xy) {
		int position = xy[0]  + xy[1]* 14;
		return position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		// TODO Auto-generated method stub
		/*if (isEnd && !isRepeat) {
			Toast toast = Toast.makeText(mContext, "游戏已经结束，重新开一局吧~", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}*/
		int[] clickXY = postion2XY(pos);
		if (isValid(mData.get(pos))) {

			if (whoMove) {
				mData.get(pos).who = 1;
				// 改变棋盘二维数组的数据
				chessBoard[clickXY[1]][clickXY[0]] = 1;
			} else {
				mData.get(pos).who = 2;
				// 改变棋盘二维数组的数据
				chessBoard[clickXY[1]][clickXY[0]] = 2;
			}
			if (!isRepeat) {
				dataRepeat.add(pos);
			}
			whoMove = !whoMove;
			if (whoMove) {
				textview_notify.setText("请黑方落子");
			} else {
				textview_notify.setText("请白方落子");
			}
			adapter.notifyDataSetChanged();
			if (!isContinue(clickXY)) {
				String winner = (whoMove == true ? "白方获胜" : "黑方获胜");
				@SuppressWarnings("deprecation")
				Builder builder = new AlertDialog.Builder(mContext,
						AlertDialog.THEME_HOLO_LIGHT);
				builder.setTitle("提示");
				builder.setMessage("游戏结束, " + winner);
				builder.setPositiveButton("确认",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						});
				builder.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						});
				builder.create();
				builder.show();
			}else{
                if(isAIOpen && !whoMove && !isRepeat){
                    int positionAI = doAICalculate(clickXY);
                    onItemClick(chessGrid, null, positionAI, positionAI);
                }
            }
		}
	}
	
	/**
     * 人工智能算法，最终返回计算机将要落子的坐标XY的position位置
     * @param who
     * @param xy
     * @return
     */
    public int doAICalculate(int[] xy){
        //可落子方案集合
        set.addAll(doAIConsider(2, xy));
        TreeSet<AIPosition> temps = new TreeSet<AIPosition>();
        for(AIPosition aiPosition : set){
        	xy = postion2XY(aiPosition.position);
			if (chessBoard[xy[1]][xy[0]]  == 0) {
				temps.add(aiPosition);
			}
		}
        set = returnTopTen(temps);
        Log.e(TAG, "defense system.......................");
		ArrayList<AIPosition> list = new ArrayList<AIPosition>();
		list.addAll(set);
        Log.e(TAG, list.toString());
        if(set != null && set.size() > 0){

            //再写一个算法，不是每次都在同等级的Level数值下选择最后一个
            return filtrateAIPosition(set,xy);
        }else{
            Toast.makeText(this, "人工智能已无棋可走", Toast.LENGTH_SHORT).show();
            //对方落子点附近无棋可落，默认返回0，
            // 会跳过此次人工智能走棋步骤，后期需要修改
            return 0;
        }
    }

    /**
     * 防卫系统：筛选可走落子点
     * @return
     */
    public int filtrateAIPosition(TreeSet<AIPosition> treeSet,int blackxy[]){
        /*for(AIPosition aiPosition : treeSet){
            int[] xy = postion2XY(aiPosition.position);
            int blankX = xy[1];
            int blankY = xy[0];
            //如果可落子点附近有对方棋子，则优先级+0.1
            float d = isHaveOpponent(1, blankX, blankY);
            aiPosition.level += d;
        }*/
		ArrayList<AIPosition> list = new ArrayList<AIPosition>();
		list.addAll(treeSet);
		treeSet.clear();
		treeSet.addAll(list);
		list.clear();
		list.addAll(treeSet);
        Log.e(TAG, list.toString());
        int []xy = null;
        if (dataRepeat.size() >1) {
        	xy = postion2XY(dataRepeat.get(dataRepeat.size()-2));
		}
        if (xy != null && isAIOpen) {
            attakSet.addAll(doAIConsider(1, xy));
            TreeSet<AIPosition> set = new TreeSet<AIPosition>();            
            for(AIPosition aiPosition : attakSet){
            	xy = postion2XY(aiPosition.position);
				if (chessBoard[xy[1]][xy[0]]  == 0) {
					/*float d = isHaveOpponent(2, xy[1], xy[0]);
		            aiPosition.level += d;*/
					set.add(aiPosition);//取10个点足够
				}
			}
            attakSet.clear();
            attakSet.addAll(set);
            attakSet = returnTopTen(attakSet);
            attackSets.add(attakSet);
            treeSet.addAll(attakSet);
            treeSet = returnTopTen(treeSet);
            Log.e(TAG, "attack system.....");
    		list = new ArrayList<AIPosition>();
    		list.addAll(attakSet);
            Log.e(TAG, list.toString());
            
		}
        Log.e(TAG, "whole system.....");
		list = new ArrayList<AIPosition>();
		list.addAll(treeSet);
        Log.e(TAG, list.toString());

        Log.e(TAG, "the best choice.....");
        Log.e(TAG, treeSet.first().toString());
        sets.add(treeSet);
        return treeSet.first().position;
    }

    /**
     * 判断可落子点附近是否有对方棋子
     * @param blankX
     * @param blankY
     * @param opponet 对方棋子，目前：1 ：黑棋，2：白旗 0 ：空棋
     * @return
     */
    public float isHaveOpponent(int opponet, int blankX, int blankY){
        int displayCount = 0;
        //遍历可落子点附近有多少个对方棋子，计算出优先级，用于递增
        for(int line = blankX - 1; line <= blankX + 1; line++){
            for(int col = blankY - 1; col <= blankY + 1; col++){
                if(line >= 0 && line < chessBoard.length
                        && col >= 0 && col < chessBoard[blankX].length
                        && chessBoard[line][col] == opponet){
                    displayCount++;
                }
            }
        }
        //根据对方棋子出现次数，增加优先级增量
        switch (displayCount){
            /*case 1:
                return 0.0f;
            case 2:
                return 0.1f;
            case 3:
                return 0.2f;
            case 4:
                return 0.3f;
            case 5:
                return 0.4f;
            case 6:
                return 0.5f;
            case 7:
                return 0.6f;
            case 8:
                return 0.7f;*/
            default:
                return 0.0f;
        }
    }

    /**
     * 人工智能思考
     * @param who
     * @param xy
     * @return
     */
    public TreeSet<AIPosition> doAIConsider(int who, int[] xy){
        //st 变量域
    	//按照优先等级进行落子方案的排序
        TreeSet<AIPosition> treeSet = new TreeSet<AIPosition>();
        //标记该方位是否有空当可落子
        boolean leftBlank = false;
        boolean rightBlank = false;

        boolean topBlank = false;
        boolean bottomBlank = false;

        boolean leftTopBlank = false;
        boolean rightBottomBlank = false;

        boolean leftBottomBlank = false;
        boolean rightTopBlank = false;

        Map<String, Integer> map = new HashMap<String, Integer>();
        //ed 变量域
        
        /**
         * 根据传入的坐标，四个方向(纵向，横向，撇向，捺向)找出最优位置，即米字格的各个顶点
         * */
        
        //st 传入坐标上方坐标        
        int x = xy[1];
        int y = xy[0] - 1;
        int continuousCount = 1;//优先级
        int blankCount = 0;
        int blankX = -1;
        int blankY = -1;
        /*********************纵向BEG*****************************************/
        //st 纵向
        while(y >= 0){
            if(chessBoard[x][y] == chessBoard[xy[1]][xy[0]]){
                continuousCount ++;
                y--;
                if (blankCount == 1 && y < 0) {
                	continuousCount ++;//空白棋两边都是对方棋子更加危险
                	map.put("TOP", xy2Position(new int[]{x, blankY}));
                	topBlank = true;
                	break;
				}
            }else if(chessBoard[x][y] == who){
                //上边已经遇到己方棋子
                if (blankCount == 1) {
                	map.put("TOP", xy2Position(new int[]{x, blankY}));
                	topBlank = true;
                	break;
				}
                break;
            }else if(chessBoard[x][y] == 0){
            	if (blankCount == 1) {
            		map.put("TOP", xy2Position(new int[]{x, blankY}));
                	topBlank = true;
                	break;
				}else {
					blankCount++;
					blankY = y;
				}
            	
            }
        }
        
        y = xy[0] + 1;//记录传入坐标上面坐标
        blankCount = 0;
        while(y < chessBoard[x].length){
            if(chessBoard[x][y] == chessBoard[xy[1]][xy[0]]){
                continuousCount ++;
                y++;
                if (blankCount == 1 && y == chessBoard[x].length) {
                	continuousCount ++;//空白棋两边都是对方棋子更加危险
                	map.put("BOTTOM", xy2Position(new int[]{x, blankY}));
                	bottomBlank = true;
				}
            }else if(chessBoard[x][y] == who){
                //下边已经遇到己方棋子
            	if (blankCount == 1) {
            		map.put("BOTTOM", xy2Position(new int[]{x, blankY}));
                	bottomBlank = true;
				}
                break;
            }else if(chessBoard[x][y] == 0){
            	if (blankCount == 1) {
            		map.put("BOTTOM", xy2Position(new int[]{x, blankY}));
                	bottomBlank = true;
                	break;
				} else {
					blankCount++;
					blankY = y;
				}
            	
            }
        }
        float level = continuousCount;
        if (who == 2) {
        	level += 0.1;
		}
        if(topBlank){
        	AIPosition aiPosition = new AIPosition();
        	aiPosition.position = (null == map.get("TOP")?0:map.get("TOP"));
        	aiPosition.level = level;
            treeSet.add(aiPosition);
        }

        if(bottomBlank){
        	AIPosition aiPosition = new AIPosition();
        	aiPosition.position = (null == map.get("BOTTOM")?0:map.get("BOTTOM"));
        	aiPosition.level = level;
            treeSet.add(aiPosition);
        }
        //ed 纵向
        /*********************纵向END*****************************************/
        
        x = xy[1] - 1;
        y = xy[0];
        continuousCount = 1;
        blankCount = 0;
        /*********************横向BEG*****************************************/
        //st 横向
        while(x >= 0){
            if(chessBoard[x][y] == chessBoard[xy[1]][xy[0]]){
                continuousCount ++;
                x--;
            	if (blankCount == 1 && x < 0) {
                	continuousCount ++;//空白棋两边都是对方棋子更加危险
            		map.put("LEFT", xy2Position(new int[]{blankX, y}));
                	leftBlank = true;
				}
            }else if(chessBoard[x][y] == who){
                //上边已经遇到己方棋子
            	if (blankCount == 1) {
            		map.put("LEFT", xy2Position(new int[]{blankX, y}));
                	leftBlank = true;
				}
                break;
            }else if(chessBoard[x][y] == 0){            	
            	if (blankCount == 1) {
            		map.put("LEFT", xy2Position(new int[]{blankX, y}));
                	leftBlank = true;
                	break;
				} else {
					blankCount++;
					blankX = x;
				}
            }
        }

        x = xy[1] + 1;
        blankCount = 0;
        blankX = -1;
        while(x < chessBoard.length){
            if(chessBoard[x][y] == chessBoard[xy[1]][xy[0]]){
                continuousCount ++;
                x++;
            	if (blankCount == 1 && x == chessBoard.length) {
                	continuousCount ++;//空白棋两边都是对方棋子更加危险
                	map.put("RIGHT", xy2Position(new int[]{blankX, y}));
                	rightBlank = true;
                	break;					
				}
            }else if(chessBoard[x][y] == who){
            	if (blankCount == 1) {
                	map.put("RIGHT", xy2Position(new int[]{blankX, y}));
                	rightBlank = true;
                	break;					
				}
                //右边已经遇到己方棋子
                break;
            }else if(chessBoard[x][y] == 0){
            	if (blankCount == 1) {
                	map.put("RIGHT", xy2Position(new int[]{blankX, y}));
                	rightBlank = true;
                	break;					
				} else {
					blankCount++;
					blankX = x;
				}
            }
        }

        level = continuousCount;
        if (who == 2) {
        	level += 0.1;
		}
        if(leftBlank){
        	AIPosition aiPosition = new AIPosition();
        	aiPosition.position = (null == map.get("LEFT")?0:map.get("LEFT"));
        	aiPosition.level = level;
            treeSet.add(aiPosition);
        }
        if(rightBlank){
        	AIPosition aiPosition = new AIPosition();
        	aiPosition.position = (null == map.get("RIGHT")?0:map.get("RIGHT"));
        	aiPosition.level = level;
            treeSet.add(aiPosition);
        }
        //ed 横向
        /*********************横向END*****************************************/
        
        x = xy[1] - 1;
        y = xy[0] - 1;
        continuousCount = 1;
        blankCount = 0;
        /*********************捺向BEG*****************************************/
        //st 捺向
        while(x >= 0 && y >= 0){
            if(chessBoard[x][y] == chessBoard[xy[1]][xy[0]]){
                continuousCount ++;
                x--;
                y--;
            	if (blankCount == 1 && (x < 0 || y <0)) {
                	continuousCount ++;//空白棋两边都是对方棋子更加危险
                	map.put("LEFTTOP", xy2Position(new int[]{blankX, blankY}));
                	leftTopBlank = true;				
				}
            }else if(chessBoard[x][y] == who){
                //左上遇到己方棋子
            	if (blankCount == 1) {
                	map.put("LEFTTOP", xy2Position(new int[]{blankX, blankY}));
                	leftTopBlank = true;				
				}
                break;
            }else if(chessBoard[x][y] == 0){
            	if (blankCount == 1) {
                	map.put("LEFTTOP", xy2Position(new int[]{blankX, blankY}));
                	leftTopBlank = true;
                	break;					
				} else {
					blankCount++;
					blankX = x;
					blankY = y;
				}
            }
        }
        
        x = xy[1] + 1;
        y = xy[0] + 1;
        blankCount = 0;
        while(x < chessBoard.length && y < chessBoard[x].length){
            if(chessBoard[x][y] == chessBoard[xy[1]][xy[0]]){
                continuousCount ++;
                x++;
                y++;            	
            	if (blankCount == 1 && (x ==  chessBoard.length || y == chessBoard[x].length)) {
                	continuousCount ++;//空白棋两边都是对方棋子更加危险
            		map.put("RIGHTBOTTOM", xy2Position(new int[]{blankX, blankY}));
                	rightBottomBlank = true;
                	break;
				}
            }else if(chessBoard[x][y] == who){
                //右下遇到己方棋子            	
            	if (blankCount == 1) {
            		map.put("RIGHTBOTTOM", xy2Position(new int[]{blankX, blankY}));
                	rightBottomBlank = true;
                	break;
				}
                break;
            }else if(chessBoard[x][y] == 0){            	
            	if (blankCount == 1) {
            		map.put("RIGHTBOTTOM", xy2Position(new int[]{blankX, blankY}));
                	rightBottomBlank = true;
                	break;
				} else {
					blankCount++;
					blankX = x;
					blankY = y;
				}
            }
        }

        level = continuousCount;
        if (who == 2) {
        	level += 0.1;
		}
        if(leftTopBlank){
            AIPosition aiPosition = new AIPosition();
            aiPosition.position = (null == map.get("LEFTTOP")?0:map.get("LEFTTOP"));
            aiPosition.level = level;
            treeSet.add(aiPosition);
        }
        
        if(rightBottomBlank){
        	AIPosition aiPosition = new AIPosition();
            aiPosition.position = (null == map.get("RIGHTBOTTOM")?0:map.get("RIGHTBOTTOM"));
            aiPosition.level = level;
            treeSet.add(aiPosition);
        }
        //ed 捺向
        /*********************捺向END*****************************************/
        
        x = xy[1] + 1;
        y = xy[0] - 1;
        continuousCount = 1;
        blankCount = 0;
        /*********************撇向BEG*****************************************/
        //st 撇向
        while(x <chessBoard.length && y >= 0){
            if(chessBoard[x][y] == chessBoard[xy[1]][xy[0]]){
                continuousCount ++;
                x++;
                y--;
            	if (blankCount == 1 && (x == chessBoard.length || y < 0)) {
                	continuousCount ++;//空白棋两边都是对方棋子更加危险
            		map.put("RIGHTTOP", xy2Position(new int[]{blankX, blankY}));
                	rightTopBlank = true;
                	break;			
				}
            }else if(chessBoard[x][y] == who){
                //右上遇到己方棋子
            	if (blankCount == 1) {
            		map.put("RIGHTTOP", xy2Position(new int[]{blankX, blankY}));
                	rightTopBlank = true;
                	break;			
				}
                break;
            }else if(chessBoard[x][y] == 0){
            	if (blankCount == 1) {
            		map.put("RIGHTTOP", xy2Position(new int[]{blankX, blankY}));
                	rightTopBlank = true;
                	break;			
				} else {
					blankCount++;
					blankX = x;
					blankY = y;
				}
            }
        }
        
        x = xy[1] - 1;
        y = xy[0] + 1;
        blankCount = 0;
        while(x >= 0 && y < chessBoard[x].length){
            if(chessBoard[x][y] == chessBoard[xy[1]][xy[0]]){
                continuousCount ++;
                x--;
                y++;       	
            	if (blankCount == 1 && (x < 0 && y == chessBoard[x].length)) {
                	continuousCount ++;//空白棋两边都是对方棋子更加危险
            		map.put("LEFTBOTTOM", xy2Position(new int[]{blankX, blankY}));
                	leftBottomBlank = true;
                	break;	
				}
            }else if(chessBoard[x][y] == who){
                //右上遇到己方棋子       	
            	if (blankCount == 1) {
            		map.put("LEFTBOTTOM", xy2Position(new int[]{blankX, blankY}));
                	leftBottomBlank = true;
                	break;	
				}
                break;
            }else if(chessBoard[x][y] == 0){            	
            	if (blankCount == 1) {
            		map.put("LEFTBOTTOM", xy2Position(new int[]{blankX, blankY}));
                	leftBottomBlank = true;
                	break;	
				} else {
					blankCount++;
					blankX = x;
					blankY = y;
				}
            }
        }

        level = continuousCount;
        if (who == 2) {
        	level += 0.1;
		}
        if(leftBottomBlank){
        	AIPosition aiPosition = new AIPosition();
            aiPosition.position = (null == map.get("LEFTBOTTOM")?0:map.get("LEFTBOTTOM"));
            aiPosition.level = level;
            treeSet.add(aiPosition);
        }

        if(rightTopBlank){
        	AIPosition aiPosition = new AIPosition();
            aiPosition.position = (null == map.get("RIGHTTOP")?0:map.get("RIGHTTOP"));
            aiPosition.level = level;
            treeSet.add(aiPosition);
        }
        //ed 撇向
        /*********************撇向END*****************************************/
        
        
        return treeSet;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged
	 * (android.widget.CompoundButton, boolean)
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		isAIOpen = isChecked;
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		final int rid = v.getId();
		switch (rid) {
		case R.id.id_regret:
			regret(v);
			break;
		case R.id.id_restart:
			restart(v);
			break;
		case R.id.id_repeat:
			repeat(v);
			break;
		}
	}
	
	/**
	 * @param set 原数据
	 * @return 截取数据
	 * */
	private TreeSet<AIPosition> returnTopTen(TreeSet<AIPosition> set) {
		if (set.size() > 10) {
			ArrayList<AIPosition> list = new ArrayList<AIPosition>();
			list.addAll(set);
			set.clear();
			set.addAll(list.subList(0, 10));
			return set;
		}else {
			return set;
		}
	}

}
