package com.example.maytinhvippro

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.maytinhvippro.databinding.ActivityMainBinding
import retrofit2.Response
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    // Khai báo các thành phần giao diện
    private lateinit var tvInput: TextView
    private lateinit var recyclerview: RecyclerView

    // Biến lưu biểu thức và phép toán
    private var expression = ""
    private var operator: String? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        tvInput = findViewById(R.id.tvInput)
        recyclerview = findViewById(R.id.recyclerview)

        val retrofitService =
            RetrofitInstance.getRetrofitInstance().create(AlbumService::class.java)

        val responseLiveData: LiveData<Response<Albums>> =
            liveData {
                val response = retrofitService.getAlbums()
                emit(response)
            }
        responseLiveData.observe(this, Observer {
            val albumList = it.body()?.listIterator()
            if (albumList != null) {
                while (albumList.hasNext()) {
                    val albumItem = albumList.next()

                    val albumTitle = "Album Title: ${albumItem.title} \n"
                    binding.tvInput.append(albumTitle)
                }
            }

        })


        // Danh sách các button
        val buttonList = listOf(
            "C",
            "%",
            "DEL",
            ":",
            "7",
            "8",
            "9",
            "x",
            "4",
            "5",
            "6",
            "-",
            "1",
            "2",
            "3",
            "+",
            "0",
            ".",
            "=",
            "Thoát"
        )

        // Thiết lập LayoutManager cho RecyclerView
        recyclerview.layoutManager =
            GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false) // 4 cột cho các button


        // Thiết lập Adapter cho RecyclerView
        recyclerview.adapter = ButtonAdapter(buttonList) { buttonText ->
            // Xử lý sự kiện click button tại đây
            Log.i("number click: ", buttonText)
            when (buttonText) {

                in "0".."9" -> appendNumber(buttonText)
                "+" -> setOperator("+")
                "-" -> setOperator("-")
                "x" -> setOperator("x")
                ":" -> setOperator(":")
                "=" -> calculateResult()
                "C" -> clearAll()
                "DEL" -> deleteLastCharacter()
                "%" -> calculatePercentage()
                "." -> appendDot()
                "Thoát" -> showExitConfirmationDialog()
            }
        }
    }

    private fun appendDot() {
        // Kiểm tra xem biểu thức hiện tại có toán tử nào không
        val parts = expression.split(" ")
        val currentNumber = if (parts.isNotEmpty()) parts.last() else ""

        // Nếu trong số hiện tại chưa có dấu chấm và số không phải là một toán tử
        if (!currentNumber.contains(".")) {
            if (currentNumber.isEmpty()) {
                // Trường hợp bắt đầu với dấu chấm, thêm "0."
                expression += "0."
            } else {
                // Thêm dấu chấm vào số hiện tại
                expression += "."
            }
            tvInput.text = expression
        }
    }

    private fun appendNumber(number: String) {
        // Thêm số vào biểu thức và hiển thị
        if (expression.isEmpty() && number == ".") {
            expression += "0." // Trường hợp người dùng bắt đầu với dấu chấm
        } else {
            expression += number
        }
        tvInput.text = expression
    }

    private fun setOperator(op: String) {
        // Kiểm tra xem biểu thức có rỗng hay không
        if (expression.isEmpty() && op == "-") {
            // Nếu biểu thức trống và phép toán là "-", thì đó là số âm
            expression += "-"
            tvInput.text = expression
        } else if (expression.isNotEmpty()) {
            if (operator != null) {
                // Nếu đã có toán tử, thay thế toán tử cũ bằng toán tử mới
                if (expression.last() != ' ') {
                    // Nếu toán tử trước không được nhập, thêm khoảng trắng
                    expression += " $op "
                } else {
                    // Thay thế toán tử cũ bằng toán tử mới
                    expression = expression.dropLast(3) + " $op "
                }
            } else {
                // Nếu chưa có toán tử nào được nhập, thêm toán tử mới
                if (expression.last() != ' ') {
                    expression += " $op "
                }
            }
            operator = op
            tvInput.text = expression
        }
    }

    private fun calculateResult() {
        // Kiểm tra xem biểu thức có rỗng hay không
        if (expression.isNotEmpty()) {
            val parts = expression.split(" ")

            // Kiểm tra xem có đủ số và toán tử hay không
            if (parts.size >= 3 && parts.size % 2 != 0) {
                // Đầu tiên, xử lý phép nhân và chia ,Danh sách này sẽ chứa kết quả tạm thời của các phép tính và những phần tử chưa được xử lý
                val newParts = mutableListOf<String>()
                //Khởi tạo biến i để duyệt qua từng phần tử trong danh sách parts.
                var i = 0
                //Vòng lặp này sẽ chạy qua tất cả các phần tử trong biểu thức, từ trái sang phải.
                while (i < parts.size) {
                    //Kiểm tra xem phần tử hiện tại (value) có phải là toán tử nhân (x) hoặc chia (:) không.
                    val value = parts[i]
                    if (value == "x" || value == ":") {
                        val left = newParts.removeAt(newParts.size - 1)
                        val right = parts[i + 1]
                        val result = when (value) {
                            //Nếu toán tử là x, thực hiện phép nhân giữa left và right.
                            "x" -> left.toDoubleOrNull()?.times(right.toDoubleOrNull() ?: 0.0)
                            //Nếu toán tử là :, thực hiện phép chia giữa left và right. Trước khi chia, kiểm tra có khác 0 hay không để tránh chia cho 0.
                            ":" -> {
                                val rightValue = right.toDoubleOrNull() ?: 0.0
                                if (rightValue != 0.0) left.toDoubleOrNull()
                                    ?.div(rightValue) else null
                            }

                            else -> null
                        }
                        newParts.add(result?.let {
                            if (it == it.toInt().toDouble()) it.toInt()
                                .toString() else it.toString()
                        } ?: "Lỗi")
                        i += 2
                    }
                    // Thực hiện các phép nhân và chia trong từ trái sang phải,lưu kết quả vào một danh sách mới (newParts).
                    // Danh sách newParts sẽ chứa các kết quả tạm thời, chuẩn bị cho các phép toán tiếp theo như cộng và trừ.
                    else {
                        newParts.add(value)
                        i++
                    }
                }

                // Sau đó, xử lý phép cộng và trừ
                var finalResult = newParts[0].toDoubleOrNull()
                //Bắt đầu từ vị trí thứ 1 vì vị trí 0 đã được sử dụng để khởi tạo finalResult. Các vị trí tiếp theo sẽ chứa toán tử và số cần tính.
                i = 1
                while (i < newParts.size) {
                    val op = newParts[i]
                    val num = newParts[i + 1].toDoubleOrNull()
                    finalResult = when (op) {
                        "+" -> finalResult?.plus(num ?: 0.0)
                        "-" -> finalResult?.minus(num ?: 0.0)
                        else -> null
                    }
                    i += 2
                }

                // Hiển thị kết quả cuối cùng
                if (finalResult != null) {
                    expression = if (finalResult == finalResult.toInt().toDouble()) {
                        finalResult.toInt().toString()
                    } else {
                        finalResult.toString()
                    }
                    tvInput.text = expression
                } else {
                    tvInput.text = "Lỗi"
                    expression = ""
                }
            } else if (parts.size == 1) {
                val num = expression.toDoubleOrNull()
                if (num != null) {
                    expression = if (num == num.toInt().toDouble()) {
                        num.toInt().toString()
                    } else {
                        num.toString()
                    }
                    tvInput.text = expression
                } else {
                    tvInput.text = "Lỗi"
                    expression = ""
                }
            } else {
                tvInput.text = "Lỗi"
                expression = ""
            }
        }
    }

    private fun clearAll() {
        expression = ""
        operator = null
        tvInput.text = ""
    }

    private fun deleteLastCharacter() {
        // Xóa ký tự cuối cùng trong biểu thức nếu không rỗng
        if (expression.isNotEmpty()) {
            // Nếu ký tự cuối là khoảng trắng, xóa cả khoảng trắng và toán tử trước đó
            if (expression.last() == ' ' && expression.length >= 3) {
                // Xóa toán tử cùng với khoảng trắng
                expression = expression.dropLast(3)
            } else {
                // Xóa ký tự cuối cùng
                expression = expression.dropLast(1)
            }

            // Cập nhật TextView tvInput với biểu thức đã cập nhật
            tvInput.text = expression
        }
    }

    private fun calculatePercentage() {
        // Kiểm tra xem biểu thức có rỗng không
        if (expression.isNotEmpty()) {
            // Nếu biểu thức kết thúc bằng toán tử, bỏ toán tử cuối cùng
            if (expression.last() == ' ') {
                expression = expression.dropLast(3)
            }

            // Thực hiện phép tính nếu có toán tử
            if (operator != null) {
                calculateResult()
            }

            // Sau khi tính toán, expression sẽ chứa kết quả cuối cùng
            val value = expression.toDoubleOrNull()

            // Nếu việc chuyển đổi thành công (giá trị không null)
            if (value != null) {
                // Tính phần trăm bằng cách chia giá trị cho 100
                val result = value / 100

                // Convert kết quả thành chuỗi mà không có ".0" nếu là số nguyên
                expression = if (result == result.toInt().toDouble()) {
                    result.toInt().toString()
                } else {
                    result.toString()
                }

                // Hiển thị biểu thức đã cập nhật trong TextView tvInput
                tvInput.text = expression
            } else {
                // Nếu chuyển đổi thất bại (giá trị là null), hiển thị lỗi
                tvInput.text = "Lỗi"
                // Xóa biểu thức vì phép toán không thành công
                expression = ""
            }
        }
    }

    private fun showExitConfirmationDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Xác nhận")
        builder.setMessage("Bạn có chắc chắn muốn thoát ứng dụng không?")

        builder.setPositiveButton("Có") { _, _ ->
            // Đóng ứng dụng
            finish()
        }

        builder.setNegativeButton("Không") { dialog, _ ->
            // Đóng hộp thoại
            dialog.dismiss()
        }

        // Hiển thị hộp thoại
        val dialog = builder.create()
        dialog.show()

    }


}

//Gửi yêu cầu (Request):gửi 1 yêu cầu http đê Url cụ thế ( y/c là GET_laydl,POST_guidl,PUT_nhapdl,DELETE_xoa || bao gòm headers, body, parameters
//Retrofit:
//Một thư viện mạnh mẽ từ Square giúp dễ dàng quản lý các yêu cầu HTTP.
// Retrofit hỗ trợ REST API, xử lý dữ liệu JSON, XML và cũng có thể sử dụng các thư viện khác như Gson hoặc Moshi để chuyển đổi dữ liệu