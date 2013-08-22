package com.persistent.bcsuite.charts.graphs;

/**
 * Enum for the graph types and the corresponding SQL queries for retrieving the
 * data from the database.
 * 
 */
public enum GraphTypes {

/*	*//**
	 * Graph for varying message size for single Publisher.
	 *//*
	PVSIZE(
			"SELECT message_size,num_publishers,num_subscribers,tput_msg_per_sec FROM (select p.token,p.message_size,num_publishers,num_subscribers,avg"
					+ "(p.tput_msg_per_sec) as tput_msg_per_sec from publisher_summary p,subscriber_summary s where s.token=p.token and s.iteration = p.iteration and "
					+ "s.num_subscribers=1 and num_publishers=1 and p.token=? group by p.token,p.message_size,num_publishers,num_subscribers) AS A;"),
*/	/**
	 * Graph for varying the number of publishers keeping message size constant
	 * at 100 bytes
	 */
	PVNUM(
			"SELECT message_size,num_publishers,num_subscribers,tput_msg_per_sec FROM (select p.token,p.message_size,num_publishers,num_subscribers,"
					+ "avg(p.tput_msg_per_sec) as tput_msg_per_sec from publisher_summary p,subscriber_summary s where s.token=p.token and s.iteration = p.iteration and "
					+ "s.num_subscribers=1 and message_size=100 and p.token=? group by p.token,p.message_size,num_publishers,num_subscribers) AS A;"),
	/**
	 * Graph for varying number of subscribers keeping the publisher count
	 * constant at 1.
	 */
	PVSUB(
			"SELECT message_size,num_publishers,num_subscribers,tput_msg_per_sec FROM (select p.token,p.message_size,num_publishers,num_subscribers,"
					+ "avg(p.tput_msg_per_sec) as tput_msg_per_sec from publisher_summary p,subscriber_summary s where s.token=p.token and s.iteration = p.iteration and "
					+ "p.num_publishers=1 and message_size=100 and p.token=? group by p.token,p.message_size,num_publishers,num_subscribers) AS A; "),

	/**
	 * Graph for varying message size for single subscriber.
	 */
	SVSIZE(
			"SELECT message_size,num_publishers,num_subscribers,subs_tput_msg_per_sec FROM (select s.token,message_size,num_publishers,num_subscribers,"
					+ "avg(s.tput_msg_per_sec) as subs_tput_msg_per_sec from publisher_summary p,subscriber_summary s where s.token=p.token and s.iteration = p.iteration and "
					+ "s.num_subscribers=1 and num_publishers=1 and s.token=? group by s.token,message_size,num_publishers,num_subscribers) AS A;"),

	/**
	 * Graph for varying the number of subscribers keeping message size constant
	 * at 100 bytes
	 */
	SVNUM(
			"SELECT message_size,num_publishers,num_subscribers,subs_tput_msg_per_sec FROM (select s.token,p.message_size,num_publishers,num_subscribers,"
					+ "avg(s.tput_msg_per_sec) as subs_tput_msg_per_sec from publisher_summary p,subscriber_summary s where s.token=p.token and s.iteration = p.iteration and"
					+ " p.num_publishers=1 and message_size=60 and p.token=? group by p.token,p.message_size,num_publishers,num_subscribers) AS A;"),
	/**
	 * Graph for varying number of publishers keeping the subscriber count
	 * constant at 1.
	 */
	SVPUB(
			"SELECT message_size,num_publishers,num_subscribers,subs_tput_msg_per_sec FROM (select s.token,p.message_size,num_publishers,num_subscribers,"
					+ "avg(s.tput_msg_per_sec) as subs_tput_msg_per_sec from publisher_summary p,subscriber_summary s where s.token=p.token and s.iteration = p.iteration and"
					+ " s.num_subscribers=1 and message_size=100 and p.token=? group by p.token,p.message_size,num_publishers,num_subscribers) AS A;"),

	/**
	 * Graph for getting the number of messages sent and lost.
	 */
	MSGCNT(
			"select 100 \"Total Sent %\",(sum(lost)/sum(sent)) *100 as \"% lost\" from (select ps.token,ps.message_size,ps.num_publishers,total_messages_sent as sent, cast(total_messages_sent as signed)- "+
	      " cast(total_messages_recd as signed) lost from publisher_summary ps ,subscriber_summary ss where ps.iteration = ss.iteration and ps.token=ss.token and ps.token=? "+
	         " and ps.num_publishers = 1 group by ps.token,ps.message_size,ps.num_publishers ) as a"),

	/**
	 * Graph for displaying the latency.
	 */
	LTNCNT(
	         "select (latency_in_nanos/2)/1000 as latency_in_nanos from publisher_detail pd where "
            + "pd.token=?"),
	/**
	 * Graph for displaying the latency percentile
	 */
	LTNPER(
			"select (latency_in_nanos/2)/1000 as latency_in_nanos from publisher_detail pd where "
					+ "pd.token=?");

	private String graphType;

	GraphTypes(String graphType) {
		this.graphType = graphType;
	}

	@Override
	public String toString() {
		return graphType;
	}

	/**
	 * Returns the corresponding graph type for a given string.
	 * 
	 * @param strGraphType
	 * @return
	 */
	public static GraphTypes fromString(String strGraphType) {
		if (strGraphType != null) {
/*			if (strGraphType.equals(PVSIZE.name())) {
				return PVSIZE;
			} else */if (strGraphType.equals(PVNUM.name())) {
				return PVNUM;
			} else if (strGraphType.equals(PVSUB.name())) {
				return PVSUB;
			} else if (strGraphType.equals(SVSIZE.name())) {
				return SVSIZE;
			} else if (strGraphType.equals(SVNUM.name())) {
				return SVNUM;
			} else if (strGraphType.equals(SVPUB.name())) {
				return SVPUB;
			} else if (strGraphType.equals(MSGCNT.name())) {
				return MSGCNT;
			} else if (strGraphType.equals(LTNCNT.name())) {
				return LTNCNT;
			}else if(strGraphType.equals(LTNPER.name())){
				return LTNPER;
			}
		}
		return null;
	}
}
