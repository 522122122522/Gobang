/**
 * 
 */
package com.game.chess.spar;

import java.util.ArrayList;
import java.util.HashMap;
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
	private ArrayList<int[]> repeatData = new ArrayList<int[]>();
	private boolean isRepeat = false;
	private boolean isRepeatEnd = true;
	private int[][] chessBoard = null;
	private GridView chessGrid = null;
	TextView textview_notify;
	CheckBox checkboxAI;
	// 是否开启人工智能 true为开启
	private boolean isAIOpen = true;
	/** 默认黑棋先手 */
	private boolean whoMove = true;

	// 记录已经刚刚棋子的坐标
	private int[] repeatBlackXY = null;
	// 记录已经机器人刚刚棋子的坐标
	private int[] repeatWhiteXY = null;
	// 记录机器人可选最优落子点集合
	TreeSet<AIPosition> attakSet = new TreeSet<AIPosition>();

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
		chessGrid.setEnabled(true);
		adapter.notifyDataSetChanged();
		isRepeat = false;
		isRepeatEnd = true;
		whoMove = true;
		repeatWhiteXY = null;
		repeatBlackXY = null;
		attakSet.clear();
		repeatData.clear();
	}

	/**
	 * 悔棋操作
	 * */
	public void regret(View view) {
		if (isRepeat) {
			Toast toast = Toast.makeText(mContext, "游戏完结啦，重新开始吧~", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}else if (repeatBlackXY == null && whoMove) {
			Toast toast = Toast.makeText(mContext, "您还没下棋呐~", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}else if (repeatWhiteXY == null && !whoMove && !isAIOpen) {
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
							//机器人对战模式悔棋移除两颗棋子
							if (isAIOpen && whoMove) {
								if (repeatWhiteXY != null) {
									chessBoard[repeatWhiteXY[0]][repeatWhiteXY[1]] = 0;
									repeatData.remove(repeatWhiteXY);
									int pos = xy2Position(repeatWhiteXY);
									mData.get(pos).who = 0;
									chessBoard[repeatBlackXY[0]][repeatBlackXY[1]] = 0;
									repeatData.remove(repeatBlackXY);
									pos = xy2Position(repeatBlackXY);
									mData.get(pos).who = 0;
								}
							}else if (whoMove) {
								chessBoard[repeatWhiteXY[0]][repeatWhiteXY[1]] = 0;
								repeatData.remove(repeatWhiteXY);
								int pos = xy2Position(repeatWhiteXY);
								mData.get(pos).who = 0;
							}else {
								chessBoard[repeatBlackXY[0]][repeatBlackXY[1]] = 0;
								repeatData.remove(repeatBlackXY);
								int pos = xy2Position(repeatBlackXY);
								mData.get(pos).who = 0;
							}
							
							whoMove = !whoMove;
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
		chessGrid.setEnabled(false);
		adapter.notifyDataSetChanged();
		isRepeat = true;
		isRepeatEnd = false;
		whoMove = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				repeatFor: for (int[] xy : repeatData) {
					int position = xy2Position(xy);
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
			}
		}).start();
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			onItemClick(chessGrid, null, msg.arg1, msg.arg1);
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
	 * 判断游戏是否继续
	 * 
	 * @return
	 */
	public boolean isContinue(int[] xy) {
		int continuousCount = 1;
		// 左
		int x = xy[0];
		int y = xy[1] - 1;

		while (y >= 0 && chessBoard[x][y] == chessBoard[xy[0]][xy[1]]) {
			continuousCount++;
			y--;
		}
		// 右
		x = xy[0];
		y = xy[1] + 1;
		while (y < chessBoard[x].length
				&& chessBoard[x][y] == chessBoard[xy[0]][xy[1]]) {
			continuousCount++;
			y++;
		}

		if (continuousCount >= 5) {
			return false;
		}

		// 上
		x = xy[0] - 1;
		y = xy[1];
		continuousCount = 1;
		while (x >= 0 && chessBoard[x][y] == chessBoard[xy[0]][xy[1]]) {
			continuousCount++;
			x--;
		}
		// 下
		x = xy[0] + 1;
		y = xy[1];
		while (x < chessBoard.length
				&& chessBoard[x][y] == chessBoard[xy[0]][xy[1]]) {
			continuousCount++;
			x++;
		}

		if (continuousCount >= 5) {
			return false;
		}

		// 左上
		x = xy[0] - 1;
		y = xy[1] - 1;
		continuousCount = 1;
		while (x >= 0 && y >= 0 && chessBoard[x][y] == chessBoard[xy[0]][xy[1]]) {
			continuousCount++;
			x--;
			y--;
		}
		// 右下
		x = xy[0] + 1;
		y = xy[1] + 1;
		while (x < chessBoard.length && y < chessBoard[x].length
				&& chessBoard[x][y] == chessBoard[xy[0]][xy[1]]) {
			continuousCount++;
			x++;
			y++;
		}

		if (continuousCount >= 5) {
			return false;
		}

		// 右上
		x = xy[0] - 1;
		y = xy[1] + 1;
		continuousCount = 1;
		while (x >= 0 && y < chessBoard[x].length
				&& chessBoard[x][y] == chessBoard[xy[0]][xy[1]]) {
			continuousCount++;
			x--;
			y++;
		}
		// 左下
		x = xy[0] + 1;
		y = xy[1] - 1;
		while (x < chessBoard.length && y >= 0
				&& chessBoard[x][y] == chessBoard[xy[0]][xy[1]]) {
			continuousCount++;
			x++;
			y--;
		}

		if (continuousCount >= 5) {
			return false;
		}

		return true;
	}

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
		int position = xy[0] * 14 + xy[1];
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
		// TODO Auto-generated method stub
		int[] clickXY = null;
		if (whoMove) {
			clickXY = repeatBlackXY = postion2XY(pos);
		}else {
			clickXY = repeatWhiteXY = postion2XY(pos);
		} 
		if (isValid(mData.get(pos))) {

			if (whoMove) {
				mData.get(pos).who = 1;
				// 改变棋盘二维数组的数据
				chessBoard[clickXY[0]][clickXY[1]] = 1;
			} else {
				mData.get(pos).who = 2;
				// 改变棋盘二维数组的数据
				chessBoard[clickXY[0]][clickXY[1]] = 2;
			}
			if (!isRepeat) {
				repeatData.add(clickXY);
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
        TreeSet<AIPosition> defenSet = doAIConsider(2, xy);
        if(defenSet != null && defenSet.size() > 0){

            //再写一个算法，不是每次都在同等级的Level数值下选择最后一个
            return filtrateAIPosition(defenSet,xy);
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
        List<AIPosition> listAIPosition = new ArrayList<AIPosition>();
        for(AIPosition aiPosition : treeSet){
            int[] xy = postion2XY(aiPosition.position);
            int blankX = xy[0];
            int blankY = xy[1];
            //如果可落子点附近有对方棋子，则优先级+0.1
            float d = isHaveOpponent(1, blankX, blankY);
            aiPosition.level += d;
        }
        //重新排序递增了优先级的落子方案
        TreeSet<AIPosition> resultTreeSet = new TreeSet<AIPosition>();
        //优先防守，进攻机会更好选进攻
        resultTreeSet.addAll(treeSet);

        if (repeatWhiteXY != null && isAIOpen) {
            attakSet.addAll(doAIConsider(1, repeatWhiteXY));
            TreeSet<AIPosition> set = new TreeSet<AIPosition>();
            
            for(AIPosition aiPosition : attakSet){
            	int[] xy = postion2XY(aiPosition.position);
				if (chessBoard[xy[0]][xy[1]]  == 0) {
					set.add(aiPosition);
				}
			}
            attakSet = set;
            resultTreeSet.addAll(attakSet);
		}

        listAIPosition.addAll(resultTreeSet);
        return listAIPosition.get(listAIPosition.size() - 1).position;
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
            case 1:
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
                return 0.7f;
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

        //左
        int x = xy[0];
        int y = xy[1] - 1;
        int continuousCount = 1;
        int blankCount = 0;
        int blankX = -1;
        int blankY = -1;

        while(y >= 0){
            if(chessBoard[x][y] == chessBoard[xy[0]][xy[1]]){
                continuousCount ++;
                y--;
            }else if(chessBoard[x][y] == who){
                //左边已经遇到己方棋子
                break;
            }else if(chessBoard[x][y] == 0){
            	if (blankCount == 1 || y==0) {
            		if (y==0) {
            			blankX = x;
    					blankY = y;
					}
	                //左边遇到空棋子
	                leftBlank = true;
	                map.put("LEFT", xy2Position(new int[]{blankX, blankY}));
	                break;
				}else {
					blankX = x;
					blankY = y;
	                y--;
				}
            	blankCount++;
            }
        }

        //右
        x = xy[0];
        y = xy[1] + 1;
        blankCount = 0;
        blankX = -1;
        blankY = -1;

        while(y < chessBoard[x].length){
            if(chessBoard[x][y] == chessBoard[xy[0]][xy[1]]){
                continuousCount ++;
                y++;
            }else if(chessBoard[x][y] == who){
                //左边已经遇到己方棋子
                break;
            }else if(chessBoard[x][y] == 0){
                if (blankCount == 1 || y == chessBoard[x].length-1) {
                	if (y == chessBoard[x].length-1) {
            			blankX = x;
    					blankY = y;
					}
                    //左边遇到空棋子
                    rightBlank = true;
                    map.put("RIGHT", xy2Position(new int[]{blankX, blankY}));
                    break;
				}else {
					blankX = x;
					blankY = y;
					y++;
				}
                blankCount++;
            }
        }

        if(leftBlank || rightBlank){
            AIPosition aiPosition1 = null;
            AIPosition aiPosition2 = null;
            //需要算法细化
            if(leftBlank){
                aiPosition1 = new AIPosition();
                aiPosition1.position = map.get("LEFT");
                aiPosition1.level = continuousCount;
            }
            if(rightBlank){
                aiPosition2 = new AIPosition();
                aiPosition2.position = map.get("RIGHT");
                aiPosition2.level = continuousCount;
            }
            if(aiPosition1 != null){
                treeSet.add(aiPosition1);
            }
            if(aiPosition2 != null){
                treeSet.add(aiPosition2);
            }
        }

        //上
        x = xy[0] - 1;
        y = xy[1];
        continuousCount = 1;
        blankCount = 0;
        blankX = -1;
        blankY = -1;

        while(x >= 0){
            if(chessBoard[x][y] == chessBoard[xy[0]][xy[1]]){
                continuousCount ++;
                x--;
            }else if(chessBoard[x][y] == who){
                //上边已经遇到己方棋子
                break;
            }else if(chessBoard[x][y] == 0){
            	if (blankCount == 1 || x == 0) {
                	if (x == 0) {
            			blankX = x;
    					blankY = y;
					}
                    //上边遇到空棋子
                    topBlank = true;
                    map.put("TOP", xy2Position(new int[]{blankX, blankY}));
                    break;
				}else {
			        blankX = x;
			        blankY = y;
			        x--;
				}
		        blankCount ++;
            }
        }

        //下
        x = xy[0] + 1;
        y = xy[1];
        blankCount = 0;
        blankX = -1;
        blankY = -1;

        while(x < chessBoard.length){
            if(chessBoard[x][y] == chessBoard[xy[0]][xy[1]]){
                continuousCount ++;
                x++;
            }else if(chessBoard[x][y] == who){
                //下边已经遇到己方棋子
                break;
            }else if(chessBoard[x][y] == 0){
            	if (blankCount == 1 || x == chessBoard.length-1) {
                	if (x == chessBoard.length-1) {
            			blankX = x;
    					blankY = y;
					}
            		//下边遇到空棋子
                    bottomBlank = true;
                    map.put("BOTTOM", xy2Position(new int[]{blankX, blankY}));
                    break;
				}else {
			        blankX = x;
			        blankY = y;
			        x++;
				}
		        blankCount ++;
            }
        }

        if(topBlank || bottomBlank){
            AIPosition aiPosition1 = null;
            AIPosition aiPosition2 = null;
            //需要算法细化
            if(topBlank){
                aiPosition1 = new AIPosition();
                aiPosition1.position = map.get("TOP");
                aiPosition1.level = continuousCount;
            }

            if(bottomBlank){
                aiPosition2 = new AIPosition();
                aiPosition2.position = map.get("BOTTOM");
                aiPosition2.level = continuousCount;
            }
            if(aiPosition1 != null){
                treeSet.add(aiPosition1);
            }
            if(aiPosition2 != null){
                treeSet.add(aiPosition2);
            }
        }

        //左上
        x = xy[0] - 1;
        y = xy[1] - 1;
        continuousCount = 1;
        blankCount = 0;
        blankX = -1;
        blankY = -1;

        while(x >= 0 && y >= 0){
            if(chessBoard[x][y] == chessBoard[xy[0]][xy[1]]){
                continuousCount ++;
                x--;
                y--;
            }else if(chessBoard[x][y] == who){
                //左上遇到己方棋子
                break;
            }else if(chessBoard[x][y] == 0){
            	if (blankCount == 1 || x==0 || y==0) {
                	if (x==0 || y==0) {
            			blankX = x;
    					blankY = y;
					}
                    //左上遇到空棋子
                    leftTopBlank = true;
                    map.put("LEFTTOP", xy2Position(new int[]{blankX, blankY}));
                    break;
				}else {
			        blankX = x;
			        blankY = y;
			        x--;
	                y--;
				}
		        blankCount ++;
            }
        }

        //右下
        x = xy[0] + 1;
        y = xy[1] + 1;
        blankCount = 0;
        blankX = -1;
        blankY = -1;

        while(x < chessBoard.length && y < chessBoard[x].length){
            if(chessBoard[x][y] == chessBoard[xy[0]][xy[1]]){
                continuousCount ++;
                x++;
                y++;
            }else if(chessBoard[x][y] == who){
                //右下遇到己方棋子
                break;
            }else if(chessBoard[x][y] == 0){
            	if (blankCount == 1 || x == chessBoard.length-1 || y == chessBoard[x].length-1) {
                	if (x == chessBoard.length-1 || y == chessBoard[x].length-1) {
            			blankX = x;
    					blankY = y;
					}
                    //右下遇到空棋子
                    rightBottomBlank = true;
                    map.put("RIGHTBOTTOM", xy2Position(new int[]{blankX, blankY}));
                    break;
				}else {
			        blankX = x;
			        blankY = y;
			        x++;
	                y++;
				}
		        blankCount ++;
            }
        }

        if(leftTopBlank || rightBottomBlank){
            AIPosition aiPosition1 = null;
            AIPosition aiPosition2 = null;
            //需要算法细化
            if(leftTopBlank){
                aiPosition1 = new AIPosition();
                aiPosition1.position = map.get("LEFTTOP");
                aiPosition1.level = continuousCount;
            }
            if(rightBottomBlank){
                aiPosition2 = new AIPosition();
                aiPosition2.position = map.get("RIGHTBOTTOM");
                aiPosition2.level = continuousCount;
            }
            if(aiPosition1 != null){
                treeSet.add(aiPosition1);
            }
            if(aiPosition2 != null){
                treeSet.add(aiPosition2);
            }
        }

        //左下
        x = xy[0] + 1;
        y = xy[1] - 1;
        continuousCount = 1;
        blankCount = 0;
        blankX = -1;
        blankY = -1;

        while(x <chessBoard.length && y >= 0){
            if(chessBoard[x][y] == chessBoard[xy[0]][xy[1]]){
                continuousCount ++;
                x++;
                y--;
            }else if(chessBoard[x][y] == who){
                //左下遇到己方棋子
                break;
            }else if(chessBoard[x][y] == 0){
            	if (blankCount == 1 || x == chessBoard.length-1 || y == 0) {
            		if (x == chessBoard.length-1 || y == 0) {
            			blankX = x;
    					blankY = y;
					}
                    //左下遇到空棋子
                    leftBottomBlank = true;
                    map.put("LEFTBOTTOM", xy2Position(new int[]{blankX, blankY}));
                    break;
				}else {
			        blankX = x;
			        blankY = y;
			        x++;
	                y--;
				}
		        blankCount ++;
            }
        }

        //右上
        x = xy[0] - 1;
        y = xy[1] + 1;
        blankCount = 0;
        blankX = -1;
        blankY = -1;

        while(x >= 0 && y < chessBoard[x].length){
            if(chessBoard[x][y] == chessBoard[xy[0]][xy[1]]){
                continuousCount ++;
                x--;
                y++;
            }else if(chessBoard[x][y] == who){
                //右上遇到己方棋子
                break;
            }else if(chessBoard[x][y] == 0){
            	if (blankCount == 1 || x == 0 || y == chessBoard[x].length-1) {
            		if (x == 0 || y == chessBoard[x].length-1) {
            			blankX = x;
    					blankY = y;
					}
                    //右上遇到空棋子
                    rightTopBlank = true;
                    map.put("RIGHTTOP", xy2Position(new int[]{blankX, blankY}));
                    break;
				}else {
			        blankX = x;
			        blankY = y;
			        x--;
	                y++;
				}
		        blankCount ++;
            }
        }

        if(leftBottomBlank || rightTopBlank){
            AIPosition aiPosition1 = null;
            AIPosition aiPosition2 = null;
            //需要算法细化
            if(leftBottomBlank){
                aiPosition1 = new AIPosition();
                aiPosition1.position = map.get("LEFTBOTTOM");
                aiPosition1.level = continuousCount;
            }
            if(rightTopBlank){
                aiPosition2 = new AIPosition();
                aiPosition2.position = map.get("RIGHTTOP");
                aiPosition2.level = continuousCount;
            }
            if(aiPosition1 != null){
                treeSet.add(aiPosition1);
            }
            if(aiPosition2 != null){
                treeSet.add(aiPosition2);
            }
        }
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

}
