package com.jonathan.financetracker.ui.components

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathan.financetracker.R
import com.jonathan.financetracker.ui.theme.FinanceTrackerTheme

//import com.google.firebase.example.makeitso.ui.theme.DarkBlue

@Composable
fun AuthWithGoogleButton(@StringRes label: Int, onButtonClick: () -> Unit) {
   OutlinedButton(
       modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
       onClick = onButtonClick,
       colors = ButtonDefaults.outlinedButtonColors(
           containerColor = Color.White,
           contentColor = MaterialTheme.colorScheme.surface
       ),
       border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.google_g),
            modifier = Modifier.padding(horizontal = 16.dp),
            contentDescription = "Google logo"
        )

        Text(
            text = stringResource(label),
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}

@Composable
fun StandardButton(@StringRes label: Int, onButtonClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        onClick = onButtonClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = stringResource(label),
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}

@Composable
fun DeleteButton(@StringRes label: Int, onButtonClick: () -> Unit) {
    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        onClick = onButtonClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = stringResource(label),
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}

@Preview (
    name = "Light Mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
fun Examplepreview() {
    FinanceTrackerTheme {
        Column (
            modifier = Modifier.padding(32.dp)) {
            StandardButton(
                label = R.string.delete_transaction,
                onButtonClick = {}
            )
            DeleteButton (
                label = R.string.delete_transaction,
                onButtonClick = {}
            )
        }

    }

}