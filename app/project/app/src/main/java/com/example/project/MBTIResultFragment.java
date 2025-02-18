package com.example.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.json.JSONObject;

public class MBTIResultFragment extends Fragment {
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mbti_result, container, false);
        
        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            String mbtiType = args.getString("mbti_type");
            try {
                JSONObject scores = new JSONObject(args.getString("scores"));
                displayResults(mbtiType, scores);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Setup retake button
        Button retakeButton = view.findViewById(R.id.retake_test_button);
        retakeButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new MBTITestFragment())
                    .commit();
        });
        
        return view;
    }

    private void displayResults(String mbtiType, JSONObject scores) {
        try {
            // Display MBTI type
            TextView typeText = view.findViewById(R.id.mbti_type_text);
            typeText.setText(mbtiType);

            // Display MBTI description
            TextView descriptionText = view.findViewById(R.id.mbti_description);
            String description = getMbtiDescription(mbtiType);
            if (description != null) {
                descriptionText.setText(description);
            } else {
                descriptionText.setText("鏈煡鐨凪BTI绫诲瀷");
            }

            // Update dimension scores
            updateDimensionScores(scores);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMbtiDescription(String mbtiType) {
        switch (mbtiType) {
            case "ISTJ":
                return "灏借矗鐨勬鏌ヨ€咃細瀹夐潤銆佷弗鑲冦€侀€氳繃鍏ㄩ潰鎬у拰鍙潬鎬ц幏寰楁垚鍔熴€傚疄闄咃紝鏈夊簭锛屾敞閲嶄簨瀹烇紝璐熻矗浠汇€傜悊鎬у湴鍐冲畾搴斿仛鐨勪簨锛屽苟鍧氭寔涓嶆噲鍦板埌杈剧洰鏍囷紝涓嶅彈骞叉壈銆?";
            case "ISFJ":
                return "灏借亴鐨勫畧鎶よ€咃細瀹夐潤銆佸弸鍠勩€佹湁璐ｄ换蹇冨拰璋ㄦ厧銆傚潥瀹氬湴鎵挎媴璐ｄ换銆傜粏蹇冦€佸噯纭€佹湁鏉＄悊銆傚繝璇氥€佷綋璐达紝鐣欏績鍜岃浣忎粬浜虹殑鐗瑰緛鍜屽叧蹇冪殑浜嬶紝鍏冲績浠栦汉鐨勬劅鍙椼€?";
            case "INFJ":
                return "瀵屾湁娲炶鐨勭悊鎯充富涔夎€咃細瀵绘眰鎬濇兂銆佸叧绯汇€佺墿璐ㄧ瓑涔嬮棿鐨勬剰涔夊拰鑱旂郴銆傚笇鏈涗簡瑙ｄ粈涔堣兘澶熸縺鍔变汉锛屽浠栦汉鏈夊緢寮虹殑娲炲療鍔涖€傚敖璐ｏ紝鍧氭寔鑷繁鐨勪环鍊艰銆?";
            case "INTJ":
                return "鐙珛鐨勬垬鐣ュ锛氬湪瀹炵幇鑷繁鐨勬兂娉曞拰杈炬垚鑷繁鐨勭洰鏍囨椂鏈夊垱鏂扮殑鎯虫硶鍜岄潪鍑＄殑鍔ㄥ姏銆傝兘寰堝揩娲炲療鍒板鐣屼簨鐗╃殑瑙勫緥骞跺舰鎴愰暱鏈熺殑杩滄櫙璁″垝銆?";
            case "ISTP":
                return "鐏垫椿鐨勫垎鏋愯€咃細鍐烽潤鐨勬梺瑙傝€咃紝瀹夐潤锛岄鐣欎綑鍦帮紝瑙傚療骞跺垎鏋愮敓娲讳腑鐨勪汉鍜屼簨鐗╋紝鍏锋湁鐞嗚В鍏惰繍浣滃師鐞嗙殑濂藉蹇冦€傚枩娆㈠啋闄╁拰鍔ㄦ墜瀹炶返銆?";
            case "ISFP":
                return "澶氭墠澶氳壓鐨勮壓鏈锛氬畨闈欍€佸弸鍠勩€佹晱鎰熴€佷翰鍒囥€備韩鍙楀綋鍓嶃€傚枩娆㈣嚜宸辩殑绌洪棿锛屽枩娆㈣嚜宸辩殑鏃堕棿琛ㄣ€傚繝浜庤嚜宸辩殑浠峰€艰锛屽繝浜庤嚜宸卞叧蹇冪殑浜恒€?";
            case "INFP":
                return "瀵屾湁鍚屾儏蹇冪殑鐞嗘兂涓讳箟鑰咃細鐞嗘兂涓讳箟鑰咃紝蹇犱簬鑷繁鐨勪环鍊艰鍜岃嚜宸卞叧蹇冪殑浜恒€傚鍦ㄧ殑鐢熸椿涓庡唴鍦ㄧ殑浠峰€艰閰嶅悎銆傚ソ濂囧績閲嶏紝寰堝揩鐪嬪埌鍚勭鍙兘鎬с€?";
            case "INTP":
                return "閫昏緫鐨勬€濇兂瀹讹細瀵绘眰鍦ㄨ嚜宸辨劅鍏磋叮鐨勪换浣曚簨鐗╀腑鎵惧嚭鍚堢悊鐨勮В閲娿€傛湁鐞嗚鎬у拰鎶借薄鎬х殑鍏磋叮锛屾洿澶氱殑鏄€濊€冭€屼笉鏄ぞ浜や簰鍔ㄣ€傚畨闈欍€佸唴鏁涖€佺伒娲汇€侀€傚簲鍔涘己銆?";
            case "ESTP":
                return "娲昏穬鐨勬寫鎴樿€咃細鐏垫椿銆佸瀹癸紝閲囧彇瀹炵敤鐨勬柟娉曚互姹傜珛鍗冲緱鍒扮粨鏋溿€傜悊璁哄拰姒傚康鎬х殑瑙ｉ噴浼氫娇浠栦滑鎰熷埌鍘岀儲锛屼粬浠兂瑕佺Н鏋佸湴閲囧彇琛屽姩瑙ｅ喅闂銆?";
            case "ESFP":
                return "闅忔€х殑琛ㄦ紨鑰咃細澶栧悜銆佸弸鍠勩€佹帴鍙楁€у己銆傜儹鐖辩敓娲汇€佷汉绫诲拰鐗╄川涓婄殑浜彈銆傚枩娆笌浠栦汉涓€璧峰皢浜嬫儏鍋氭垚銆傚湪宸ヤ綔涓姹傚父璇嗗拰瀹炵敤鎬с€?";
            case "ENFP":
                return "鐑儏鐨勫垱鎰忓锛氱儹鎯呮磱婧€€佸瘜鏈夋兂璞″姏銆傝涓轰汉鐢熸湁澶鐨勫彲鑳芥€с€傝兘寰堝揩鍦板皢浜嬫儏鍜屼俊鎭仈绯昏捣鏉ワ紝骞舵牴鎹嚜宸辩殑鍒ゆ柇鑷俊鍦拌В鍐抽棶棰樸€?";
            case "ENTP":
                return "澶ц儐鐨勬€濇兂瀹讹細鍙嶅簲蹇€佺澘鏅猴紝鏈夋縺鍔变粬浜虹殑鍔ㄥ姏锛岃瑙夋€у己銆佺洿瑷€涓嶈銆傚湪瑙ｅ喅鏂扮殑銆佸叿鏈夋寫鎴樻€х殑闂鏃舵満鏅虹伒娲汇€傚杽浜庢壘鍑虹悊璁轰笂鐨勫彲鑳芥€с€?";
            case "ESTJ":
                return "楂樻晥鐨勭鐞嗚€咃細瀹為檯銆佺幇瀹炰富涔夎€咃紝鍏锋湁浼佷笟鎴栨妧鏈柟闈㈢殑澶╄祴銆備笉鎰熷叴瓒ｇ殑棰嗗煙涓嶅弬涓庯紝浣嗗湪蹇呰鏃朵細鎶曞叆銆傚枩娆㈢粍缁囧拰棰嗗娲诲姩銆?";
            case "ESFJ":
                return "鍜屽杽鐨勭収椤捐€咃細鐑績銆佹湁璐ｄ换蹇冦€佸悎浣溿€傚笇鏈涘懆鍥寸殑鐜娓╅Θ鑰屽拰璋愶紝骞朵负姝ゆ灉鏂湴鎵ц銆傚枩娆笌浠栦汉涓€璧风簿纭苟鍙婃椂鍦板畬鎴愪换鍔°€?";
            case "ENFJ":
                return "瀵屾湁鍚屾儏蹇冪殑寮曞鑰咃細娓╂殩銆佸悓鎯呭績寮恒€佹湁璐ｄ换蹇冦€佸瘜鏈夋劅鏌撳姏鐨勯瀵艰€呫€傚鍒汉鎵€璇寸殑鍜屾墍闇€瑕佺殑鏁忔劅锛岃兘寰堝ソ鍦颁负涓汉鎴栫兢浣撴湇鍔°€?";
            case "ENTJ":
                return "鏋滄柇鐨勬寚鎸ュ畼锛氬潶鐜囥€佹灉鏂紝鏈夊ぉ鐢熺殑棰嗗鑳藉姏銆傝兘寰堝揩鐪嬪埌鍏徃/缁勭粐绋嬪簭涓殑涓嶅悎鐞嗘€у拰浣庢晥鎬э紝鍙戝睍骞跺疄鏂芥湁鏁堝拰鍏ㄩ潰鐨勭郴缁熸潵瑙ｅ喅闂銆?";
            default:
                return "鏈煡鐨凪BTI绫诲瀷";
        }
    }

    private void updateDimensionScores(JSONObject scores) {
        try {
            // E/I Dimension
            TextView eiText = view.findViewById(R.id.ei_score_text);
            ProgressBar eiProgress = view.findViewById(R.id.ei_progress);
            eiText.setText(getString(R.string.mbti_dimension_ei));
            eiProgress.setMax(scores.getInt("E") + scores.getInt("I"));
            eiProgress.setProgress(scores.getInt("E"));

            // S/N Dimension
            TextView snText = view.findViewById(R.id.sn_score_text);
            ProgressBar snProgress = view.findViewById(R.id.sn_progress);
            snText.setText(getString(R.string.mbti_dimension_sn));
            snProgress.setMax(scores.getInt("S") + scores.getInt("N"));
            snProgress.setProgress(scores.getInt("S"));

            // T/F Dimension
            TextView tfText = view.findViewById(R.id.tf_score_text);
            ProgressBar tfProgress = view.findViewById(R.id.tf_progress);
            tfText.setText(getString(R.string.mbti_dimension_tf));
            tfProgress.setMax(scores.getInt("T") + scores.getInt("F"));
            tfProgress.setProgress(scores.getInt("T"));

            // J/P Dimension
            TextView jpText = view.findViewById(R.id.jp_score_text);
            ProgressBar jpProgress = view.findViewById(R.id.jp_progress);
            jpText.setText(getString(R.string.mbti_dimension_jp));
            jpProgress.setMax(scores.getInt("J") + scores.getInt("P"));
            jpProgress.setProgress(scores.getInt("J"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 