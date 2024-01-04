package com.feduss.telegram.entity.consts

sealed class Section(val baseRoute: String, val parametricRoute: String = "") {
    data object LoginWelcomePage: Section("LoginWelcomePage")
    data object LoginAuthChoice: Section("LoginAuthChoice")
    data object LoginAuthPhoneNumber: Section("LoginAuthPhoneNumber")
    data object LoginAuthQrCode: Section("LoginAuthQrCode")
    data object LoginOtpVerification: Section("LoginOtpVerification")
    data object Login2FAVerification: Section("Login2FAVerification")
    data object ChatList: Section("ChatList")
    data object ChatHistory: Section("ChatHistory", "ChatHistory/{${Params.ChatId.name}}")
    //object Setup: Section("setup")
    //object Edit: Section("edit", "edit/{tag}")
    //object Timer: Section("timer", "timer?chipIndex={chipIndex}&cycleIndex={cycleIndex}&timerSeconds={timerSeconds}")

    fun withArgs(args: List<String>? = null, optionalArgs: Map<String, String>? = null): String {
        var destinationRoute = baseRoute
        args?.let { argsNotNull ->
            for(arg in argsNotNull) {
                destinationRoute += "/$arg"
            }
        }
        optionalArgs?.let { optionalArgsNotNull ->
            destinationRoute+= "?"
            optionalArgsNotNull.onEachIndexed { index, (optionalArgName, optionaArgValue) ->
                destinationRoute += "$optionalArgName=$optionaArgValue"

                if (optionalArgsNotNull.count() > 1 && index < optionalArgsNotNull.count() - 1) {
                    destinationRoute += "&"
                }
            }
        }
        return destinationRoute
    }
}
