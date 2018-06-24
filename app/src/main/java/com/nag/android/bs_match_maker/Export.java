package com.nag.android.bs_match_maker;
import android.app.Activity;
import android.content.Intent;

public class Export {

	public static void Execute(Activity activity, Player[] players) {
		StringBuilder sb=new StringBuilder();
		ExportTitle(sb);
		for(Player player : players){
			ExportOne(sb, player);
		}
		Export(activity, sb.toString());
	}

	private static void ExportTitle(StringBuilder sb)
	{
		sb.append(" Rank, Player, Match Point, Opponent Point, Win Point");
	}

	private static void ExportOne(StringBuilder sb, Player player)
	{
		sb.append(player.getRank());
		sb.append(", \"");
		sb.append(player.getName());
		sb.append("\", ");
		sb.append(player.getMatchPercentage());
		sb.append(", ");
		sb.append(player.getOpponentPercentage());
		sb.append(", ");
		sb.append(player.getWinPoint());
		sb.append("\r\n");
	}
	private static void Export(Activity activity, String report)
	{
		Intent intent = new Intent();

		intent.setAction(Intent.ACTION_SEND);
		intent.setType("message/rfc822");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"foo@example.com"});
		intent.putExtra(Intent.EXTRA_SUBJECT, "BS Match Maker Result");
		intent.putExtra(Intent.EXTRA_TEXT, report);
		activity.startActivity(intent);
	}
}
